package com.example.traffic_management.service;

import com.example.traffic_management.config.SimProperties;
import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.entity.Route;
import com.example.traffic_management.mapper.PoiMapper;
import com.example.traffic_management.mapper.RouteMapper;
import com.example.traffic_management.util.AmapRouteClient;
import com.example.traffic_management.util.PolylineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路线服务：负责为「工厂 → 工厂」生成并缓存路线实体，
 * 以及为车辆接单段（任意位置 → 工厂）生成临时路线。
 *
 * <p>几何来源优先级：高德驾车路径规划（配置了 Web 服务 key 且可用）→ 合成折线。
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 合成路线标记 */
    public static final String ROAD_TYPE_SYNTHETIC = "城市道路";
    /** 由前端 JS API 规划出的真实道路标记 */
    public static final String ROAD_TYPE_NAVIGATION = "导航路线";

    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private PoiMapper poiMapper;
    @Autowired
    private AmapRouteClient amapClient;
    @Autowired
    private SimProperties props;

    /** 接单段临时路线缓存：key = vehicleId:missionId */
    private final Map<String, List<double[]>> legCache = new ConcurrentHashMap<>();

    /** 路线折线点缓存：routeId -> points，避免每辆车每轮都回表 + 解析 JSON */
    private final Map<Integer, List<double[]>> routePointsCache = new ConcurrentHashMap<>();

    /** 高德不可用时置位，避免每轮都去请求失败 */
    private volatile boolean amapBroken = false;

    /** 工厂之间的干线路线：命中缓存返回，否则生成并落库 */
    public Route ensureFactoryRoute(Factory from, Factory to) {
        String startPoi = ensurePoi(from);
        String endPoi = ensurePoi(to);

        Route cached = routeMapper.findByPoiPair(startPoi, endPoi);
        if (cached != null && cached.getKeyPoint() != null && !cached.getKeyPoint().isBlank()) {
            return cached;
        }

        List<double[]> points = generate(from.getFactoryLatitude(), from.getFactoryLongitude(),
                to.getFactoryLatitude(), to.getFactoryLongitude(),
                Objects.hash(from.getId(), to.getId()));

        Route route = new Route();
        route.setStartPointName(from.getFactoryName());
        route.setEndPointName(to.getFactoryName());
        route.setStartPoint(startPoi);
        route.setEndPoint(endPoi);
        // generate() 若走了高德，amapBroken 保持 false，这里就能正确标记来源
        route.setRoadType(amapEnabled() ? ROAD_TYPE_NAVIGATION : ROAD_TYPE_SYNTHETIC);
        double km = PolylineUtil.totalKm(points);
        route.setDistance(scaled(km));
        route.setKeyPoint(writePoints(points));
        route.setEstimatedTime((int) Math.max(1, Math.round(km)));
        route.setStatus("可用");
        route.setCreateTime(new Date());
        routeMapper.insert(route);

        log.info("生成路线 {}({}) → {}({})：{} km，{} 个折线点",
                from.getFactoryName(), startPoi, to.getFactoryName(), endPoi,
                route.getDistance(), points.size());
        return route;
    }

    public Route getById(Integer id) {
        return routeMapper.findById(id);
    }

    /** 路线长度（km），取不到返回 null */
    public Double routeDistance(Integer routeId) {
        if (routeId == null) {
            return null;
        }
        Route route = routeMapper.findById(routeId);
        return route == null ? null : route.getDistance();
    }

    /**
     * 前端用高德 JS API 规划出真实道路后回写折线。
     *
     * @return 影响行数，0 表示路线不存在或点数不足
     */
    public int updateGeometry(Integer routeId, List<double[]> points) {
        if (routeId == null || points == null || points.size() < 2) {
            return 0;
        }
        Route route = routeMapper.findById(routeId);
        if (route == null) {
            return 0;
        }
        double km = PolylineUtil.totalKm(points);
        route.setKeyPoint(writePoints(points));
        route.setDistance(scaled(km));
        route.setEstimatedTime((int) Math.max(1, Math.round(km)));
        route.setRoadType(ROAD_TYPE_NAVIGATION);
        int updated = routeMapper.updateGeometry(route);
        if (updated > 0) {
            routePointsCache.put(routeId, points);
        }
        log.info("路线 {} 已更新为导航路线：{} km，{} 个折线点", routeId, route.getDistance(), points.size());
        return updated;
    }

    /** 路线折线点，供前端绘制 */
    public List<double[]> pointsOf(Integer routeId) {
        if (routeId == null) {
            return new ArrayList<>();
        }
        List<double[]> cached = routePointsCache.get(routeId);
        if (cached != null) {
            return cached;
        }
        Route route = routeMapper.findById(routeId);
        if (route == null) {
            return new ArrayList<>();
        }
        List<double[]> points = readPoints(route.getKeyPoint());
        if (points.size() >= 2) {
            routePointsCache.put(routeId, points);
        }
        return points;
    }

    /** 车辆接单段（当前位置 → 装货工厂）的临时路线，按车辆+任务缓存 */
    public List<double[]> pickupPath(Integer vehicleId, Integer missionId,
                                     double lat, double lng, Factory target) {
        String key = vehicleId + ":" + missionId;
        return legCache.computeIfAbsent(key, k -> generate(lat, lng,
                target.getFactoryLatitude(), target.getFactoryLongitude(), k.hashCode()));
    }

    public void clearPickupPath(Integer vehicleId) {
        legCache.keySet().removeIf(k -> k.startsWith(vehicleId + ":"));
    }

    /**
     * 前端用高德 JS API 规划出「车辆当前位置 → 装货工厂」的真实道路后回写，
     * 让接单行驶这一段也走真实路线。
     */
    public void setPickupPath(Integer vehicleId, Integer missionId, List<double[]> points) {
        if (vehicleId == null || missionId == null || points == null || points.size() < 2) {
            return;
        }
        legCache.put(vehicleId + ":" + missionId, points);
        log.info("车辆 {} 的取货路线已更新为真实道路：{} 个折线点", vehicleId, points.size());
    }

    /** 取货段当前折线（供调试/前端读取） */
    public List<double[]> pickupPoints(Integer vehicleId, Integer missionId) {
        return legCache.getOrDefault(vehicleId + ":" + missionId, new ArrayList<>());
    }

    public void clearCache() {
        legCache.clear();
        routePointsCache.clear();
    }

    // ------------------------------------------------------------------

    /** 高德 Web 服务是否可用（配置了 key 且尚未失败过） */
    private boolean amapEnabled() {
        String key = props.getAmapWebKey();
        return !amapBroken && key != null && !key.isBlank();
    }

    private String ensurePoi(Factory factory) {
        String id = "F" + factory.getId();
        poiMapper.insertIfAbsent(id, "工厂",
                factory.getFactoryLongitude() + "," + factory.getFactoryLatitude(), "装卸货点");
        return id;
    }

    private List<double[]> generate(double lat1, double lng1, double lat2, double lng2, long seed) {
        String key = props.getAmapWebKey();
        if (!amapBroken && key != null && !key.isBlank()) {
            try {
                List<double[]> points = amapClient.driving(lat1, lng1, lat2, lng2, key);
                if (points.size() >= 2) {
                    return points;
                }
            } catch (Exception e) {
                amapBroken = true;
                log.warn("高德路径规划不可用（{}）。后续使用合成路线；请确认 sim.amap-web-key 是「Web服务」类型的 key。",
                        e.getMessage());
            }
        }
        return PolylineUtil.synthetic(lat1, lng1, lat2, lng2, seed);
    }

    private String writePoints(List<double[]> points) {
        try {
            return JSON.writeValueAsString(points);
        } catch (Exception e) {
            throw new IllegalStateException("路线折线序列化失败", e);
        }
    }

    private List<double[]> readPoints(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            double[][] array = JSON.readValue(json, double[][].class);
            List<double[]> points = new ArrayList<>(array.length);
            for (double[] point : array) {
                points.add(point);
            }
            return points;
        } catch (Exception e) {
            log.warn("路线折线解析失败：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private static Double scaled(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
