package com.example.traffic_management.service;

import com.example.traffic_management.config.SimProperties;
import com.example.traffic_management.dto.SimStateDto;
import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.entity.Mission;
import com.example.traffic_management.entity.Route;
import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.enums.MissionStatus;
import com.example.traffic_management.enums.VehicleState;
import com.example.traffic_management.mapper.FactoryMapper;
import com.example.traffic_management.mapper.MissionMapper;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.util.GeoUtil;
import com.example.traffic_management.util.PolylineUtil;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 马表仿真主循环。
 *
 * <p>每轮（默认 5 秒 = 现实 5 分钟）：
 * <ol>
 *   <li>按概率生成一条新的运输任务（mission）；</li>
 *   <li>推进每辆车的状态计时：状态没结束就扣减剩余时间并沿直线移动；</li>
 *   <li>状态在本轮结束时，按马尔科夫转移表抽样下一个状态；</li>
 *   <li>根据新状态绑定任务、计算耗时，最后统一落库。</li>
 * </ol>
 */
@Service
public class SimulationService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    /** 仿真生成任务时随机挑选的货物类型 */
    private static final String[] GOODS_TYPES = {
            "木材", "钢材", "水泥", "粮食", "家具", "电器", "塑料制品", "酒水"
    };

    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private MissionMapper missionMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private VehicleStateService stateService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private SimProperties props;
    @Autowired
    private PlatformTransactionManager transactionManager;

    /** 整轮仿真放进一个事务：50 辆车约上百条 UPDATE，逐条自动提交会被 fsync 拖垮 */
    private TransactionTemplate transactionTemplate;

    private final Random random = new Random();
    private final AtomicInteger missionSeq = new AtomicInteger();

    private volatile boolean running = false;
    private volatile int tick = 0;
    private volatile int simMinutes = 0;
    /** 工厂经纬度缓存：仿真中频繁取坐标，避免每轮查库 */
    private volatile Map<Integer, Factory> factoryIndex;

    private Thread simThread;

    @Override
    public void afterPropertiesSet() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        log.info("马表仿真服务已就绪：{} 秒/轮，每轮 {} 分钟，车速 {} km/h",
                props.getTickSeconds(), props.getMinutesPerTick(), props.getSpeedKmh());
    }

    // ------------------------------------------------------------------
    // 生命周期
    // ------------------------------------------------------------------

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        simThread = new Thread(this::runLoop, "sim-loop");
        simThread.setDaemon(true);
        simThread.start();
        log.info("马表仿真已启动");
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (simThread != null) {
            simThread.interrupt();
        }
        log.info("马表仿真已停止，共执行 {} 轮", tick);
    }

    /** 停止并清空任务、车辆回到空闲状态 */
    public synchronized void reset() {
        running = false;
        if (simThread != null) {
            simThread.interrupt();
            simThread = null;
        }
        int removed = missionMapper.deleteAll();
        vehicleMapper.resetAllStates();
        tick = 0;
        simMinutes = 0;
        factoryIndex = null;
        routeService.clearCache();

        // 把车辆停到真实工厂点位上：起点在路网上，第一段接单行驶才不会从"野地"窜出来
        List<Factory> parking = factories();
        int moved = 0;
        if (!parking.isEmpty()) {
            for (Vehicle v : vehicleMapper.selectAllNotDeleted()) {
                Factory spot = parking.get(random.nextInt(parking.size()));
                v.setCurrentAction(VehicleState.IDLE.getActionId());
                v.setRemainingTime(0);
                v.setDistanceToPickup(0.0);
                v.setTransportDistance(0.0);
                v.setCurrentLatitude(spot.getFactoryLatitude());
                v.setCurrentLongitude(spot.getFactoryLongitude());
                vehicleMapper.update(v);
                moved++;
            }
        }
        log.info("仿真已重置：删除 {} 条任务，{} 辆车停靠到工厂点位", removed, moved);
    }

    @PreDestroy
    public void shutdown() {
        stop();
    }

    public boolean isRunning() {
        return running;
    }

    public int getTick() {
        return tick;
    }

    public int getSimMinutes() {
        return simMinutes;
    }

    // ------------------------------------------------------------------
    // 主循环
    // ------------------------------------------------------------------

    private void runLoop() {
        while (running) {
            try {
                tickOnce();
            } catch (Exception e) {
                log.error("仿真第 {} 轮执行异常", tick + 1, e);
            }
            try {
                Thread.sleep(props.getTickSeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
    }

    /** 执行一轮仿真，独立方法便于测试时手动触发 */
    public void tickOnce() {
        transactionTemplate.executeWithoutResult(status -> doTick());
    }

    private synchronized void doTick() {
        tick++;
        simMinutes += props.getMinutesPerTick();

        List<Vehicle> vehicles = vehicleMapper.selectAllNotDeleted();
        generateMissions(vehicles);

        // 一次性加载所有进行中的任务，按车辆分组，避免每辆车一次查询
        Map<Integer, Mission> activeByCar = new HashMap<>();
        for (Mission m : missionMapper.findAllActive()) {
            if (m.getCarId() != null) {
                activeByCar.put(m.getCarId(), m);
            }
        }

        for (Vehicle v : vehicles) {
            try {
                advance(v, activeByCar.get(v.getId()));
            } catch (Exception e) {
                log.warn("车辆 {} 状态推进失败", v.getId(), e);
            }
        }

        // 车辆多了以后逐车日志太吵，改成每分钟（默认 12 轮）打一条汇总
        if (tick % 12 == 0) {
            long idle = vehicles.stream()
                    .filter(v -> VehicleState.fromActionId(v.getCurrentAction()) == VehicleState.IDLE)
                    .count();
            log.info("第 {} 轮 | 仿真 {} 分钟 | 车辆 {} 辆（空闲 {}，在途 {}）| 待接单 {}",
                    tick, simMinutes, vehicles.size(), idle, vehicles.size() - idle,
                    missionMapper.countPending());
        }
    }

    /**
     * 按「空闲车辆缺口」补充运输任务：空闲车比待接单任务多多少，本轮就最多补几条。
     * 这样 50 辆车也能持续有货可拉，而不是开局把任务抢空后集体闲置。
     */
    private void generateMissions(List<Vehicle> vehicles) {
        long idle = vehicles.stream()
                .filter(v -> VehicleState.fromActionId(v.getCurrentAction()) == VehicleState.IDLE)
                .count();
        int pending = missionMapper.countPending();
        int shortage = (int) Math.max(0, idle - pending);
        int room = Math.max(0, props.getMaxPendingTasks() - pending);
        int quota = Math.min(Math.min(shortage, props.getTasksPerTick()), room);
        for (int i = 0; i < quota; i++) {
            if (random.nextDouble() > props.getTaskProbability()) {
                continue;
            }
            createMission();
        }
    }

    /** 生成一条运输任务：随机取两个工厂作为起点/终点，路线长度按实际折线计算 */
    private void createMission() {
        List<Factory> factories = factories();
        if (factories.size() < 2) {
            return;
        }
        Factory origin = factories.get(random.nextInt(factories.size()));
        Factory dest = origin;
        while (dest.getId().equals(origin.getId())) {
            dest = factories.get(random.nextInt(factories.size()));
        }
        // 先生成/复用路线实体，任务的距离取路线实际长度（而不是两点直线距离）
        Route route = routeService.ensureFactoryRoute(origin, dest);
        double km = route.getDistance() != null
                ? route.getDistance()
                : GeoUtil.distanceKm(origin.getFactoryLatitude(), origin.getFactoryLongitude(),
                dest.getFactoryLatitude(), dest.getFactoryLongitude());

        Mission mission = new Mission();
        mission.setMissionNo("M" + System.currentTimeMillis() + "-" + missionSeq.incrementAndGet());
        mission.setGoodsType(GOODS_TYPES[random.nextInt(GOODS_TYPES.length)]);
        mission.setFactoryId(origin.getId());
        mission.setDestFactoryId(dest.getId());
        mission.setRouteId(route.getId());
        mission.setDistanceKm(scaled(km, 1));
        mission.setTotalMinutes(stateService.durationMinutes(VehicleState.TRANSPORTING, km)
                + props.getLoadingMinutes() + props.getUnloadingMinutes());
        mission.setElapsedMinutes(0);
        mission.setQuantity(1);
        mission.setStatus(MissionStatus.PENDING);
        mission.setCreateTime(new Date());
        missionMapper.insert(mission);

        log.debug("生成新任务 {}：{} → {}，货物={}，{} km",
                mission.getMissionNo(), origin.getFactoryName(), dest.getFactoryName(),
                mission.getGoodsType(), mission.getDistanceKm());
    }

    /**
     * 推进一辆车：状态未结束则扣时间并移动；状态结束则抽样下一状态并重新绑定任务。
     */
    private void advance(Vehicle v, Mission mission) {
        int step = props.getMinutesPerTick();
        VehicleState current = VehicleState.fromActionId(v.getCurrentAction());
        int remaining = v.getRemainingTime() == null ? 0 : v.getRemainingTime();
        Integer beforeAction = v.getCurrentAction();
        Integer beforeRemaining = v.getRemainingTime();
        Double beforeLat = v.getCurrentLatitude();
        Double beforeLng = v.getCurrentLongitude();

        if (remaining > step) {
            // 状态延续：只走时间，不调用状态选择函数
            v.setRemainingTime(remaining - step);
            if (current.isMoving() && mission != null) {
                moveStep(v, current, mission);
            }
        } else {
            // 状态在本轮结束：若刚卸完货，先结算任务
            if (current == VehicleState.UNLOADING && mission != null) {
                completeMission(mission);
                mission = null;
            }
            VehicleState next = stateService.next(current, random);
            mission = applyTransition(v, next, mission);
            // 进入行驶状态的这一轮先不动：给前端一个 tick 的时间把真实道路折线回写进来，
            // 否则车辆会先沿后端合成的折线窜出去一段，再被纠正到路网上。
            log.debug("车辆{}：{} → {}，剩余 {} 分钟{}", v.getId(), current.getDesc(), next.getDesc(),
                    v.getRemainingTime(),
                    mission == null ? "" : "，任务 " + mission.getMissionNo());
        }

        if (mission != null && current.isWorking()) {
            mission.setElapsedMinutes(nz(mission.getElapsedMinutes()) + step);
            missionMapper.updateProgress(mission);
        }
        // 空闲且没有任何变化的车辆不必回写
        boolean changed = !Objects.equals(beforeAction, v.getCurrentAction())
                || !Objects.equals(beforeRemaining, v.getRemainingTime())
                || !Objects.equals(beforeLat, v.getCurrentLatitude())
                || !Objects.equals(beforeLng, v.getCurrentLongitude());
        if (changed) {
            vehicleMapper.update(v);
        }
    }

    /**
     * 落实一次状态转移：绑定/沿用任务，计算该状态时长。
     *
     * @return 转移后车辆负责的任务（可能为新绑定的任务，也可能为 null）
     */
    private Mission applyTransition(Vehicle v, VehicleState next, Mission mission) {
        // 离开接单行驶就说明这一趟取货路线走完了，清掉临时路线缓存
        if (next != VehicleState.ACCEPTING) {
            routeService.clearPickupPath(v.getId());
        }
        switch (next) {
            case IDLE -> park(v);
            case WAITING -> {
                v.setCurrentAction(VehicleState.WAITING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.WAITING, 0));
            }
            case ACCEPTING -> {
                mission = ensureMission(v, mission);
                if (mission == null) {
                    park(v);
                    return null;
                }
                double pickupKm = distanceToFactory(v, mission.getFactoryId());
                v.setCurrentAction(VehicleState.ACCEPTING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.ACCEPTING, pickupKm));
                v.setDistanceToPickup(scaled(pickupKm, 1));
                v.setTransportDistance(scaled(nzd(mission.getDistanceKm()), 1));
                mission.setStatus(MissionStatus.ACCEPTING);
            }
            case LOADING -> {
                mission = ensureMission(v, mission);
                if (mission == null) {
                    park(v);
                    return null;
                }
                v.setCurrentAction(VehicleState.LOADING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.LOADING, 0));
                mission.setStatus(MissionStatus.LOADING);
            }
            case TRANSPORTING -> {
                mission = ensureMission(v, mission);
                if (mission == null) {
                    park(v);
                    return null;
                }
                // 路线可能已被前端替换为真实道路，按数据库里的最新长度计算耗时
                Double routeKm = routeService.routeDistance(mission.getRouteId());
                double km = (routeKm != null && routeKm > 0) ? routeKm : nzd(mission.getDistanceKm());
                double delta = km - nzd(mission.getDistanceKm());
                mission.setDistanceKm(scaled(km, 1));
                if (Math.abs(delta) > 0.05) {
                    mission.setTotalMinutes((int) Math.round(nz(mission.getTotalMinutes()) + delta));
                }
                v.setCurrentAction(VehicleState.TRANSPORTING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.TRANSPORTING, km));
                v.setTransportDistance(scaled(km, 1));
                mission.setStatus(MissionStatus.TRANSPORTING);
            }
            case UNLOADING -> {
                mission = ensureMission(v, mission);
                if (mission == null) {
                    park(v);
                    return null;
                }
                v.setCurrentAction(VehicleState.UNLOADING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.UNLOADING, 0));
                mission.setStatus(MissionStatus.UNLOADING);
            }
            default -> park(v);
        }
        return mission;
    }

    /**
     * 车上没有任务时，从待接单池里挑一个距离装货点最近的任务。
     */
    private Mission ensureMission(Vehicle v, Mission mission) {
        if (mission != null) {
            return mission;
        }
        List<Mission> pending = missionMapper.findPending(20);
        if (pending.isEmpty()) {
            return null;
        }
        // 按取货距离从近到远尝试，抢单成功的才归这辆车
        pending.sort(Comparator.comparingDouble(m -> distanceToFactory(v, m.getFactoryId())));

        for (Mission best : pending) {
            if (missionMapper.claim(best.getId(), v.getId()) != 1) {
                // 已被其他车辆抢走，看下一条
                continue;
            }
            double pickupKm = distanceToFactory(v, best.getFactoryId());
            best.setCarId(v.getId());
            best.setStatus(MissionStatus.ACCEPTING);
            best.setTotalMinutes(nz(best.getTotalMinutes())
                    + stateService.durationMinutes(VehicleState.ACCEPTING, pickupKm));

            Factory origin = factory(best.getFactoryId());
            Factory dest = factory(best.getDestFactoryId());
            log.debug("车辆{} 接单 {}：{} → {}，取货距离 {} km",
                    v.getId(), best.getMissionNo(),
                    origin == null ? best.getFactoryId() : origin.getFactoryName(),
                    dest == null ? best.getDestFactoryId() : dest.getFactoryName(),
                    scaled(pickupKm, 1));
            return best;
        }
        return null;
    }

    private void completeMission(Mission mission) {
        mission.setStatus(MissionStatus.DONE);
        missionMapper.updateProgress(mission);
        log.debug("任务 {} 已完成", mission.getMissionNo());
    }

    private void park(Vehicle v) {
        v.setCurrentAction(VehicleState.IDLE.getActionId());
        v.setRemainingTime(0);
    }

    /** 沿路线折线朝当前状态的目标点前进一个 tick 对应的里程 */
    private void moveStep(Vehicle v, VehicleState state, Mission mission) {
        double stepKm = props.getSpeedKmh() * props.getMinutesPerTick() / 60.0;

        List<double[]> path;
        if (state == VehicleState.ACCEPTING) {
            Factory origin = factory(mission.getFactoryId());
            if (origin == null) {
                return;
            }
            double lat = v.getCurrentLatitude() == null ? origin.getFactoryLatitude() : v.getCurrentLatitude();
            double lng = v.getCurrentLongitude() == null ? origin.getFactoryLongitude() : v.getCurrentLongitude();
            path = routeService.pickupPath(v.getId(), mission.getId(), lat, lng, origin);
        } else {
            path = routeService.pointsOf(mission.getRouteId());
            if (path.size() < 2) {
                // 兜底：历史任务没有路线时退回两点直线
                Factory dest = factory(mission.getDestFactoryId());
                if (dest == null) {
                    return;
                }
                double lat = v.getCurrentLatitude() == null ? dest.getFactoryLatitude() : v.getCurrentLatitude();
                double lng = v.getCurrentLongitude() == null ? dest.getFactoryLongitude() : v.getCurrentLongitude();
                path = List.of(new double[]{lng, lat},
                        new double[]{dest.getFactoryLongitude(), dest.getFactoryLatitude()});
            }
        }

        double lat = v.getCurrentLatitude() == null ? path.get(0)[1] : v.getCurrentLatitude();
        double lng = v.getCurrentLongitude() == null ? path.get(0)[0] : v.getCurrentLongitude();
        double[] pos = PolylineUtil.advance(path, lat, lng, stepKm);
        v.setCurrentLatitude(scaled(pos[0], 6));
        v.setCurrentLongitude(scaled(pos[1], 6));
    }

    private double distanceToFactory(Vehicle v, Integer factoryId) {
        Factory f = factory(factoryId);
        if (f == null || f.getFactoryLatitude() == null || f.getFactoryLongitude() == null) {
            return 0;
        }
        double lat = v.getCurrentLatitude() == null ? f.getFactoryLatitude() : v.getCurrentLatitude();
        double lng = v.getCurrentLongitude() == null ? f.getFactoryLongitude() : v.getCurrentLongitude();
        return GeoUtil.distanceKm(lat, lng, f.getFactoryLatitude(), f.getFactoryLongitude());
    }

    // ------------------------------------------------------------------
    // 工厂缓存与快照
    // ------------------------------------------------------------------

    private List<Factory> factories() {
        return new ArrayList<>(factoryIndex().values());
    }

    private Factory factory(Integer id) {
        return id == null ? null : factoryIndex().get(id);
    }

    private Map<Integer, Factory> factoryIndex() {
        Map<Integer, Factory> index = factoryIndex;
        if (index == null) {
            index = new LinkedHashMap<>();
            for (Factory f : factoryMapper.selectAll()) {
                if (f.getFactoryLatitude() != null && f.getFactoryLongitude() != null) {
                    index.put(f.getId(), f);
                }
            }
            factoryIndex = index;
        }
        return index;
    }

    /** 取货段当前折线（车辆 → 装货工厂） */
    public List<double[]> pickupPoints(Integer vehicleId, Integer missionId) {
        return routeService.pickupPoints(vehicleId, missionId);
    }

    /** 前端回写取货段的真实道路折线 */
    public void updatePickupPoints(Integer vehicleId, Integer missionId, List<double[]> points) {
        routeService.setPickupPath(vehicleId, missionId, points);
    }

    /** 前端一次轮询所需的完整快照 */
    public SimStateDto state() {
        SimStateDto dto = new SimStateDto();
        dto.setRunning(running);
        dto.setTick(tick);
        dto.setSimMinutes(simMinutes);
        dto.setTickSeconds(props.getTickSeconds());

        List<Vehicle> vehicles = vehicleMapper.selectAllNotDeleted();
        dto.setVehicles(vehicles);

        Map<Integer, Vehicle> vehicleById = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleById.put(v.getId(), v);
        }

        List<Mission> active = missionMapper.findActive(20);
        for (Mission m : active) {
            m.setRemainMinutes(estimateRemainMinutes(m, vehicleById.get(m.getCarId())));
        }
        dto.setActiveMissions(active);

        dto.setPendingMissions(missionMapper.findByStatus(MissionStatus.PENDING, 10));
        dto.setDoneMissions(missionMapper.findByStatus(MissionStatus.DONE, 10));
        return dto;
    }

    /**
     * 估算任务真实剩余：当前状态剩余 + 后续阶段的固定耗时/行驶耗时。
     * 直接用 total - elapsed 会算错，因为状态自环会让实际耗时超过初始估算。
     */
    private int estimateRemainMinutes(Mission mission, Vehicle vehicle) {
        int remain = vehicle == null ? 0 : nz(vehicle.getRemainingTime());
        int transport = (int) Math.round(nzd(mission.getDistanceKm()));
        String status = mission.getStatus() == null ? "" : mission.getStatus();
        return switch (status) {
            case MissionStatus.ACCEPTING ->
                    remain + props.getLoadingMinutes() + transport + props.getUnloadingMinutes();
            case MissionStatus.LOADING -> remain + transport + props.getUnloadingMinutes();
            case MissionStatus.TRANSPORTING -> remain + props.getUnloadingMinutes();
            default -> remain;
        };
    }

    // ------------------------------------------------------------------
    // 小工具
    // ------------------------------------------------------------------

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private static double nzd(Double value) {
        return value == null ? 0.0 : value;
    }

    private static Double scaled(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
