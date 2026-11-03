package com.example.traffic_management.service;

import com.example.traffic_management.config.SimProperties;
import com.example.traffic_management.enums.VehicleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 车辆状态机：只负责「下一个状态是什么」和「这个状态要持续多久」，不访问数据库。
 * 落库与位置插值交给 {@link SimulationService}。
 */
@Service
public class VehicleStateService {

    @Autowired
    private SimProperties props;

    /** 按马尔科夫表抽样下一个状态 */
    public VehicleState next(VehicleState current, Random random) {
        return VehicleState.sampleNextState(current, random);
    }

    /**
     * 计算某个状态需要持续的分钟数。
     *
     * @param state      目标状态
     * @param distanceKm 该状态对应的行驶距离（仅接单行驶/运货行驶使用）
     */
    public int durationMinutes(VehicleState state, double distanceKm) {
        return switch (state) {
            // 60km/h：公里数 = 分钟数
            case ACCEPTING, TRANSPORTING -> Math.max(1, (int) Math.round(distanceKm / props.getSpeedKmh() * 60));
            case LOADING -> props.getLoadingMinutes();
            case UNLOADING -> props.getUnloadingMinutes();
            case WAITING -> props.getWaitingMinutes();
            case IDLE -> 0;
        };
    }
}
