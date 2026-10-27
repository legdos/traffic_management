package com.example.traffic_management.service;

import com.example.traffic_management.entity.SimulationStatus;

public interface SimulationService {
    SimulationStatus status();
    SimulationStatus start();
    SimulationStatus pause();
    SimulationStatus step();
}
