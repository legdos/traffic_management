package com.example.traffic_management.controller;

import com.example.traffic_management.dto.SimStateDto;
import com.example.traffic_management.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sim")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @PostMapping("/start")
    public String start() {
        simulationService.start();
        return "马表仿真已启动";
    }

    @PostMapping("/stop")
    public String stop() {
        simulationService.stop();
        return "马表仿真已停止";
    }

    /** 停止仿真并清空任务、车辆回到空闲，便于重新演示 */
    @PostMapping("/reset")
    public String reset() {
        simulationService.reset();
        return "仿真已重置";
    }

    /** 前端轮询入口：一次拿到轮次、车辆、任务 */
    @GetMapping("/state")
    public SimStateDto state() {
        return simulationService.state();
    }

    /** 取货段当前折线（车辆当前位置 → 装货工厂） */
    @GetMapping("/pickup/{vehicleId}/{missionId}/points")
    public List<double[]> pickupPoints(@PathVariable("vehicleId") Integer vehicleId,
                                       @PathVariable("missionId") Integer missionId) {
        return simulationService.pickupPoints(vehicleId, missionId);
    }

    /** 前端把「车辆 → 装货工厂」的真实道路折线回写，让接单段也走真路 */
    @PutMapping("/pickup/{vehicleId}/{missionId}/points")
    public String updatePickupPoints(@PathVariable("vehicleId") Integer vehicleId,
                                     @PathVariable("missionId") Integer missionId,
                                     @RequestBody List<double[]> points) {
        simulationService.updatePickupPoints(vehicleId, missionId, points);
        return "取货路线已更新";
    }
}
