package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public Vehicle findById(Integer id) {
        return vehicleMapper.selectById(id);
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleMapper.selectAll();
    }

    @Override
    public void save(Vehicle vehicle) {
        vehicleMapper.insert(vehicle);
    }

    @Override
    public void update(Vehicle vehicle) {
        vehicleMapper.update(vehicle);
    }

    @Override
    public void deleteById(Integer id) {
        vehicleMapper.deleteById(id);
    }
}