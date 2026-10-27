package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.SimulationStatus;
import com.example.traffic_management.entity.SimulationTarget;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.service.SimulationService;
import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SimulationServiceImpl implements SimulationService {
    private static final long TICK_INTERVAL_MILLIS = 1_000;
    private static final double MAX_STEP_KM = 2.0;

    private final VehicleMapper vehicleMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong ticks = new AtomicLong(0);
    private final Object tickLock = new Object();
    private volatile int movedVehicles;
    private volatile String lastError;
    private volatile Thread worker;

    public SimulationServiceImpl(VehicleMapper vehicleMapper) {
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public SimulationStatus status() { return snapshot(); }

    @Override
    public synchronized SimulationStatus start() {
        if (running.compareAndSet(false, true)) {
            Thread thread = new Thread(this::runLoop, "traffic-simulation-loop");
            thread.setDaemon(true);
            worker = thread;
            thread.start();
        }
        return snapshot();
    }

    @Override
    public synchronized SimulationStatus pause() {
        running.set(false);
        Thread thread = worker;
        if (thread != null) thread.interrupt();
        return snapshot();
    }

    @Override
    public SimulationStatus step() {
        if (running.get()) throw new ResponseStatusException(HttpStatus.CONFLICT, "自动仿真运行中，请先暂停再单步");
        synchronized (tickLock) {
            if (running.get()) throw new ResponseStatusException(HttpStatus.CONFLICT, "自动仿真运行中，请先暂停再单步");
            movedVehicles = moveAssignedVehicles();
            ticks.incrementAndGet();
            lastError = null;
            return snapshot();
        }
    }

    private void runLoop() {
        Thread thisWorker = Thread.currentThread();
        while (running.get()) {
            try {
                synchronized (tickLock) {
                    if (!running.get()) break;
                    movedVehicles = moveAssignedVehicles();
                    ticks.incrementAndGet();
                    lastError = null;
                }
            } catch (RuntimeException error) {
                lastError = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            }
            try {
                Thread.sleep(TICK_INTERVAL_MILLIS);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        if (worker == thisWorker) worker = null;
    }

    private int moveAssignedVehicles() {
        List<SimulationTarget> targets = vehicleMapper.selectAssignedSimulationTargets();
        int moved = 0;
        for (SimulationTarget target : targets) {
            if (target.getCurrentLatitude() == null || target.getCurrentLongitude() == null
                    || target.getTargetLatitude() == null || target.getTargetLongitude() == null
                    || target.getVersion() == null) continue;
            double distanceKm = haversine(target.getCurrentLatitude(), target.getCurrentLongitude(),
                    target.getTargetLatitude(), target.getTargetLongitude());
            if (distanceKm < 0.01) continue;
            double fraction = Math.min(1.0, MAX_STEP_KM / distanceKm);
            double latitude = target.getCurrentLatitude()
                    + (target.getTargetLatitude() - target.getCurrentLatitude()) * fraction;
            double longitude = target.getCurrentLongitude()
                    + (target.getTargetLongitude() - target.getCurrentLongitude()) * fraction;
            if (vehicleMapper.updatePosition(target.getId(), target.getVersion(), latitude, longitude, new Date()) == 1) moved++;
        }
        return moved;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0088 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private SimulationStatus snapshot() {
        SimulationStatus status = new SimulationStatus();
        status.setRunning(running.get());
        status.setTicks(ticks.get());
        status.setMovedVehicles(movedVehicles);
        status.setLastError(lastError);
        return status;
    }

    @PreDestroy
    public void shutdown() { pause(); }
}
