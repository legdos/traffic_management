package com.example.traffic_management.service;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.VehicleMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SimulationService implements InitializingBean {
    @Autowired private VehicleMapper vehicleMapper;
    @Autowired private VehicleStateService stateService;

    private volatile boolean running = false;
    private Thread simThread;
    private static final int SLEEP_MS = 3000;   // 模拟间隔
    private static final int CYCLE_MIN = 5;     // 代表现实5分钟

    // 项目启动后仅打印初始化，不循环
    @Override
    public void afterPropertiesSet() {
        System.out.println("马表仿真服务已就绪");
    }

    // Controller 调用：启动
    public void start() {
        if (running) return;
        running = true;
        simThread = new Thread(this::runLoop, "SimLoop");
        simThread.start();
    }

    // Controller 调用：停止
    public void stop() {
        running = false;
        System.out.println("仿真已停止");
    }

    private void runLoop() {
        int count = 0;
        while (running) {
            count++;
            System.out.println("===== 第 " + count + " 次马表循环 =====");
            List<Vehicle> list = vehicleMapper.selectAll();
            stateService.updateAllVehicles(list, CYCLE_MIN);
            for (Vehicle v : list) {
                vehicleMapper.update(v); // 持久化
                System.out.printf("车%d: 状态=%d, 剩余=%d分%n",
                        v.getId(), v.getCurrentAction(), v.getRemainingTime());
            }
            try { Thread.sleep(SLEEP_MS); }
            catch (InterruptedException e) { running = false; }
        }
    }
}