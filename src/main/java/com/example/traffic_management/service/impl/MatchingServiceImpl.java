package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.entity.MatchingResult;
import com.example.traffic_management.entity.Need;
import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.FactoryMapper;
import com.example.traffic_management.mapper.NeedMapper;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.service.MatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingServiceImpl implements MatchingService {
    private final NeedMapper needMapper;
    private final VehicleMapper vehicleMapper;
    private final FactoryMapper factoryMapper;

    public MatchingServiceImpl(NeedMapper needMapper, VehicleMapper vehicleMapper, FactoryMapper factoryMapper) {
        this.needMapper = needMapper;
        this.vehicleMapper = vehicleMapper;
        this.factoryMapper = factoryMapper;
    }

    @Override
    public MatchingResult score(Integer needId, Integer vehicleId) {
        Need need = needMapper.selectById(needId);
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (need == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在或已失效");
        if (vehicle == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "车辆不存在");
        MatchingResult result = new MatchingResult();
        result.setNeedId(needId); result.setVehicleId(vehicleId);
        List<String> reasons = new ArrayList<>();
        boolean eligible = vehicle.getIsDeleted() == null || vehicle.getIsDeleted() == 0;
        if (!eligible) reasons.add("车辆已失效");
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) { eligible = false; reasons.add("车辆当前状态不是 AVAILABLE"); }

        BigDecimal typeScore = BigDecimal.ONE;
        if (need.getRequiredVehicleType() != null && !need.getRequiredVehicleType().isBlank()) {
            boolean same = need.getRequiredVehicleType().equalsIgnoreCase(vehicle.getTypeCode());
            typeScore = same ? BigDecimal.ONE : BigDecimal.ZERO;
            if (!same) { eligible = false; reasons.add("车辆类型不符合需求"); }
        } else reasons.add("需求未限定车辆类型");

        BigDecimal capacityScore = BigDecimal.ZERO;
        if (need.getCargoTotalWeight() == null || need.getCargoTotalWeight().signum() <= 0) {
            eligible = false; reasons.add("需求货物重量缺失或不大于 0");
        } else if (vehicle.getMaxLoad() == null || vehicle.getMaxLoad().isBlank()) {
            eligible = false; reasons.add("车辆最大载重缺失");
        } else {
            try {
                BigDecimal maxLoad = new BigDecimal(vehicle.getMaxLoad());
                capacityScore = maxLoad.divide(need.getCargoTotalWeight(), 6, RoundingMode.HALF_UP).min(BigDecimal.ONE);
                if (maxLoad.compareTo(need.getCargoTotalWeight()) < 0) { eligible = false; reasons.add("车辆最大载重不足"); }
                else reasons.add("车辆载重满足需求");
            } catch (NumberFormatException e) { eligible = false; reasons.add("车辆最大载重不是有效数字"); }
        }

        Factory factory = factoryMapper.selectById(need.getFactoryId());
        BigDecimal distanceScore = BigDecimal.ZERO;
        if (factory != null && factory.getFactoryLatitude() != null && factory.getFactoryLongitude() != null && vehicle.getCurrentLatitude() != null && vehicle.getCurrentLongitude() != null) {
            double distance = haversine(vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude(), factory.getFactoryLatitude(), factory.getFactoryLongitude());
            result.setDistanceKm(BigDecimal.valueOf(distance).setScale(3, RoundingMode.HALF_UP));
            distanceScore = BigDecimal.ONE.subtract(result.getDistanceKm().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)).max(BigDecimal.ZERO);
            reasons.add("已按车辆到工厂的直线距离计算");
        } else reasons.add("缺少车辆或工厂坐标，距离分为 0");
        BigDecimal score = typeScore.multiply(new BigDecimal("0.4")).add(capacityScore.multiply(new BigDecimal("0.4"))).add(distanceScore.multiply(new BigDecimal("0.2")));
        if (!eligible) score = BigDecimal.ZERO;
        result.setTypeScore(typeScore.setScale(3, RoundingMode.HALF_UP)); result.setCapacityScore(capacityScore.setScale(3, RoundingMode.HALF_UP));
        result.setDistanceScore(distanceScore.setScale(3, RoundingMode.HALF_UP)); result.setScore(score.setScale(3, RoundingMode.HALF_UP));
        result.setEligible(eligible); result.setReasons(reasons);
        if (eligible) reasons.add(0, "满足车型、载重和车辆状态要求");
        return result;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0088 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
