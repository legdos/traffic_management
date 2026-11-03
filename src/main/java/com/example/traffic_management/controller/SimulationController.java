package com.example.traffic_management.controller;

import com.example.traffic_management.dto.SimStateDto;
import com.example.traffic_management.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
