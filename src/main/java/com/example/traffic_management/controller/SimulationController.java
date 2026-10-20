package com.example.traffic_management.controller;

import com.example.traffic_management.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sim")
public class SimulationController {
    @Autowired private SimulationService simulationService;

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
}