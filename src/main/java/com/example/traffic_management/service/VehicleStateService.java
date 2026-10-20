package com.example.traffic_management.service;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.enums.VehicleState;
import com.example.traffic_management.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class VehicleStateService {

    @Autowired
    private VehicleMapper vehicleMapper;

    private final Random random = new Random();

    //批量更新所有车辆状态（马表入口）
    public void updateAllVehicles(List<Vehicle> vehicles, int cycleMinutes) {
        for (Vehicle v : vehicles) {
            updateVehicle(v, cycleMinutes);
        }
    }

    /**
     * 更新单辆车状态（马表核心逻辑）
     */
    public void updateVehicle(Vehicle v, int cycleMinutes) {
        VehicleState current = VehicleState.fromActionId(v.getCurrentAction());
        int remain = v.getRemainingTime() == null ? 0 : v.getRemainingTime();
        remain -= cycleMinutes;

        // 判断是否触发状态转移（时间耗尽 或 随机概率触发）
        if (remain <= 0 || random.nextDouble() < current.getTransitionProb()) {
            VehicleState next = VehicleState.getNextState(current);

            // 关键：在转换到特定状态时，调用对应的业务函数
            switch (next) {
                case LOADING:
                    // 进入装货前，调用 selectGoods()
                    selectGoods(v);
                    break;

                case IDLE:
                case WAITING:
                    // 空闲/等待时，调用 WaitAndSearchNewNeed()
                    boolean found = waitAndSearchNewNeed(v);
                    if (found) {
                        // 找到了新需求，转为接单行驶
                        next = VehicleState.ACCEPTING;
                        System.out.println("车辆" + v.getId() + "：找到新需求，转为接单行驶");
                    }
                    break;
            }

            // 更新车辆状态
            v.setCurrentAction(next.getActionId());

            // 计算新状态的持续时间
            int duration = calculateStateDuration(next);
            v.setRemainingTime(duration);

            System.out.printf("车辆%d: %s → %s, 持续%d分钟%n",
                    v.getId(), current.getDesc(), next.getDesc(), duration);
        } else {
            // 状态未完成，只更新剩余时间
            v.setRemainingTime(remain);
        }

        // 持久化到数据库
        vehicleMapper.update(v);
    }

    /**
     * selectGoods() —— 选择货物
     * 当前仅为占位实现，后续可对接真实货物选择逻辑
     */
    private void selectGoods(Vehicle vehicle) {
        // TODO: 对接真实的货物选择逻辑
        // 当前仅打印日志，表示该函数被调用
        System.out.println("车辆" + vehicle.getId() + "：调用 selectGoods()，准备选择货物");
    }

    /**
     * WaitAndSearchNewNeed() —— 搜索新需求
     * 当前仅为占位实现，后续可对接真实需求搜索逻辑
     * @return true表示找到新需求，false表示未找到
     */
    private boolean waitAndSearchNewNeed(Vehicle vehicle) {
        // TODO: 对接真实的需求搜索逻辑
        // 当前仅打印日志，表示该函数被调用
        System.out.println("车辆" + vehicle.getId() + "：调用 WaitAndSearchNewNeed()，搜索新需求");
        return false; // 默认未找到
    }

    /**
     * 计算某个状态的持续时间（分钟）
     */
    private int calculateStateDuration(VehicleState state) {
        return state.getBaseDuration();
    }
}