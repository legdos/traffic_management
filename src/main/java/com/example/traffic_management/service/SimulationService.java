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
    private static final int SLEEP_MS = 3000;
    private static final int CYCLE_MIN = 5;

    @Override
    public void afterPropertiesSet() {
        System.out.println("马表仿真服务已就绪");
    }

    public void start() {
        if (running) return;
        running = true;
        simThread = new Thread(this::runLoop, "SimLoop");
        simThread.start();
    }

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
                // 根据状态更新位置
                updateVehiclePosition(v);

                // 持久化
                vehicleMapper.update(v);

                System.out.printf("车%d: 状态=%d, 剩余=%d分, 位置=(%.6f, %.6f)%n",
                        v.getId(), v.getCurrentAction(), v.getRemainingTime(),
                        v.getCurrentLongitude(), v.getCurrentLatitude());
            }

            try { Thread.sleep(SLEEP_MS); }
            catch (InterruptedException e) { running = false; }
        }
    }

    private void updateVehiclePosition(Vehicle v) {
        int actionId = v.getCurrentAction();

        switch (actionId) {
            case 2: // ACCEPTING
            case 4: // TRANSPORTING
                moveAlongRoute(v);
                break;
            case 1: // IDLE
                randomDrift(v);
                break;
            case 3: // LOADING
            case 5: // UNLOADING
            case 6: // WAITING
            case 7: // INIT
                break;
        }
    }

    private void moveAlongRoute(Vehicle v) {
        double angle = Math.random() * 2 * Math.PI;
        double step = 0.002;

        double newLng = v.getCurrentLongitude() + step * Math.cos(angle);
        double newLat = v.getCurrentLatitude() + step * Math.sin(angle);

        newLng = Math.max(103.8, Math.min(105.0, newLng));
        newLat = Math.max(29.5, Math.min(32.0, newLat));

        v.setCurrentLongitude(newLng);
        v.setCurrentLatitude(newLat);
    }

    private void randomDrift(Vehicle v) {
        double driftLng = (Math.random() - 0.5) * 0.002;
        double driftLat = (Math.random() - 0.5) * 0.002;

        v.setCurrentLongitude(v.getCurrentLongitude() + driftLng);
        v.setCurrentLatitude(v.getCurrentLatitude() + driftLat);
    }
}