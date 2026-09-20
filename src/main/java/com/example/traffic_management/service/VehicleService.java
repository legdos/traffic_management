package com.example.traffic_management.service;

import com.example.traffic_management.entity.Vehicle;
import java.util.List;

public interface VehicleService {
    Vehicle findById(Integer id);
    List<Vehicle> findAll();
    void save(Vehicle vehicle);
    void update(Vehicle vehicle);
    void deleteById(Integer id);
}