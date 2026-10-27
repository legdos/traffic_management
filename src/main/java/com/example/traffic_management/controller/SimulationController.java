package com.example.traffic_management.controller;

import com.example.traffic_management.entity.SimulationStatus;
import com.example.traffic_management.service.SimulationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation")
public class SimulationController {
    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) { this.simulationService = simulationService; }

    @GetMapping("/status")
    public SimulationStatus status() { return simulationService.status(); }

    @PostMapping("/start")
    public SimulationStatus start() { return simulationService.start(); }

    @PostMapping("/pause")
    public SimulationStatus pause() { return simulationService.pause(); }

    @PostMapping("/step")
    public SimulationStatus step() { return simulationService.step(); }
}
