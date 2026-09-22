package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public Vehicle findById(Integer id) { return vehicleMapper.selectById(id); }

    @Override
    public List<Vehicle> findAll() { return vehicleMapper.selectAll(); }

    @Override
    public List<Vehicle> findAllActive() {
        return vehicleMapper.selectAllNotDeleted();
    }

    @Override
    @Transactional
    public void save(Vehicle vehicle) { vehicleMapper.insert(vehicle); }

    @Override
    @Transactional
    public void update(Vehicle vehicle) { vehicleMapper.update(vehicle); }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        vehicleMapper.deleteById(id); // 保留物理删除
    }

    @Override
    @Transactional
    public void softDeleteById(Integer id) {
        vehicleMapper.softDeleteById(id); // 新增软删除
    }
}