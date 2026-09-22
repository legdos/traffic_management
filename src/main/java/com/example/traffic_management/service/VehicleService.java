package com.example.traffic_management.service;

import com.example.traffic_management.entity.Vehicle;
import java.util.List;

public interface VehicleService {
    Vehicle findById(Integer id);
    List<Vehicle> findAll();
    List<Vehicle> findAllActive();      // 查询未删除（isDeleted=0）
    void save(Vehicle vehicle);
    void update(Vehicle vehicle);
    void deleteById(Integer id);         // 保留：物理删除
    void softDeleteById(Integer id);     // 新增：软删除（isDeleted=1）
}