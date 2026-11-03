package com.example.traffic_management.service;

import com.example.traffic_management.config.SimProperties;
import com.example.traffic_management.dto.SimStateDto;
import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.entity.Mission;
import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.enums.MissionStatus;
import com.example.traffic_management.enums.VehicleState;
import com.example.traffic_management.mapper.FactoryMapper;
import com.example.traffic_management.mapper.MissionMapper;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.util.GeoUtil;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private SimProperties props;

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
        log.info("仿真已重置：删除 {} 条任务，车辆全部回到空闲", removed);
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
    public synchronized void tickOnce() {
        tick++;
        simMinutes += props.getMinutesPerTick();
        generateMission();

        List<Vehicle> vehicles = vehicleMapper.selectAllNotDeleted();
        for (Vehicle v : vehicles) {
            try {
                advance(v);
            } catch (Exception e) {
                log.warn("车辆 {} 状态推进失败", v.getId(), e);
            }
        }
    }

    /**
     * 生成一条新的运输任务：随机取两个工厂作为起点/终点，距离按经纬度计算。
     */
    private void generateMission() {
        if (random.nextDouble() > props.getTaskProbability()) {
            return;
        }
        if (missionMapper.countPending() >= props.getMaxPendingTasks()) {
            return;
        }
        List<Factory> factories = factories();
        if (factories.size() < 2) {
            return;
        }
        Factory origin = factories.get(random.nextInt(factories.size()));
        Factory dest = origin;
        while (dest.getId().equals(origin.getId())) {
            dest = factories.get(random.nextInt(factories.size()));
        }
        double km = GeoUtil.distanceKm(origin.getFactoryLatitude(), origin.getFactoryLongitude(),
                dest.getFactoryLatitude(), dest.getFactoryLongitude());

        Mission mission = new Mission();
        mission.setMissionNo("M" + System.currentTimeMillis() + "-" + missionSeq.incrementAndGet());
        mission.setGoodsType(GOODS_TYPES[random.nextInt(GOODS_TYPES.length)]);
        mission.setFactoryId(origin.getId());
        mission.setDestFactoryId(dest.getId());
        mission.setDistanceKm(scaled(km, 1));
        mission.setTotalMinutes(stateService.durationMinutes(VehicleState.TRANSPORTING, km)
                + props.getLoadingMinutes() + props.getUnloadingMinutes());
        mission.setElapsedMinutes(0);
        mission.setQuantity(1);
        mission.setStatus(MissionStatus.PENDING);
        mission.setCreateTime(new Date());
        missionMapper.insert(mission);

        log.info("生成新任务 {}：{} → {}，货物={}，{} km",
                mission.getMissionNo(), origin.getFactoryName(), dest.getFactoryName(),
                mission.getGoodsType(), mission.getDistanceKm());
    }

    /**
     * 推进一辆车：状态未结束则扣时间并移动；状态结束则抽样下一状态并重新绑定任务。
     */
    private void advance(Vehicle v) {
        int step = props.getMinutesPerTick();
        VehicleState current = VehicleState.fromActionId(v.getCurrentAction());
        Mission mission = missionMapper.findActiveByVehicleId(v.getId());
        int remaining = v.getRemainingTime() == null ? 0 : v.getRemainingTime();

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
            if (next.isMoving() && mission != null) {
                moveStep(v, next, mission);
            }
            log.info("车辆{}：{} → {}，剩余 {} 分钟{}", v.getId(), current.getDesc(), next.getDesc(),
                    v.getRemainingTime(),
                    mission == null ? "" : "，任务 " + mission.getMissionNo());
        }

        if (mission != null && current.isWorking()) {
            mission.setElapsedMinutes(nz(mission.getElapsedMinutes()) + step);
            missionMapper.updateProgress(mission);
        }
        vehicleMapper.update(v);
    }

    /**
     * 落实一次状态转移：绑定/沿用任务，计算该状态时长。
     *
     * @return 转移后车辆负责的任务（可能为新绑定的任务，也可能为 null）
     */
    private Mission applyTransition(Vehicle v, VehicleState next, Mission mission) {
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
                v.setCurrentAction(VehicleState.TRANSPORTING.getActionId());
                v.setRemainingTime(stateService.durationMinutes(VehicleState.TRANSPORTING, nzd(mission.getDistanceKm())));
                v.setTransportDistance(scaled(nzd(mission.getDistanceKm()), 1));
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
        Mission best = null;
        double bestKm = Double.MAX_VALUE;
        for (Mission candidate : pending) {
            double km = distanceToFactory(v, candidate.getFactoryId());
            if (km < bestKm) {
                bestKm = km;
                best = candidate;
            }
        }
        if (best == null) {
            return null;
        }
        best.setCarId(v.getId());
        best.setStatus(MissionStatus.ACCEPTING);
        best.setTotalMinutes(nz(best.getTotalMinutes())
                + stateService.durationMinutes(VehicleState.ACCEPTING, bestKm));

        Factory origin = factory(best.getFactoryId());
        Factory dest = factory(best.getDestFactoryId());
        log.info("车辆{} 接单 {}：{} → {}，取货距离 {} km",
                v.getId(), best.getMissionNo(),
                origin == null ? best.getFactoryId() : origin.getFactoryName(),
                dest == null ? best.getDestFactoryId() : dest.getFactoryName(),
                scaled(bestKm, 1));
        return best;
    }

    private void completeMission(Mission mission) {
        mission.setStatus(MissionStatus.DONE);
        missionMapper.updateProgress(mission);
        log.info("任务 {} 已完成", mission.getMissionNo());
    }

    private void park(Vehicle v) {
        v.setCurrentAction(VehicleState.IDLE.getActionId());
        v.setRemainingTime(0);
    }

    /** 沿直线朝当前状态的目标点前进一个 tick 对应的里程 */
    private void moveStep(Vehicle v, VehicleState state, Mission mission) {
        Integer targetId = state == VehicleState.ACCEPTING
                ? mission.getFactoryId()
                : mission.getDestFactoryId();
        Factory target = factory(targetId);
        if (target == null) {
            return;
        }
        double stepKm = props.getSpeedKmh() * props.getMinutesPerTick() / 60.0;
        double lat = v.getCurrentLatitude() == null ? target.getFactoryLatitude() : v.getCurrentLatitude();
        double lng = v.getCurrentLongitude() == null ? target.getFactoryLongitude() : v.getCurrentLongitude();

        double[] pos = GeoUtil.moveTowards(lat, lng,
                target.getFactoryLatitude(), target.getFactoryLongitude(), stepKm);
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

    /** 前端一次轮询所需的完整快照 */
    public SimStateDto state() {
        SimStateDto dto = new SimStateDto();
        dto.setRunning(running);
        dto.setTick(tick);
        dto.setSimMinutes(simMinutes);
        dto.setVehicles(vehicleMapper.selectAllNotDeleted());
        dto.setPendingMissions(missionMapper.findByStatus(MissionStatus.PENDING, 10));
        dto.setActiveMissions(missionMapper.findActive(10));
        dto.setDoneMissions(missionMapper.findByStatus(MissionStatus.DONE, 10));
        return dto;
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
