package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // 查询所有车辆（含已删除）
    @GetMapping
    public List<Vehicle> findAll() {
        return vehicleService.findAll();
    }

    // 查询未删除的车辆
    @GetMapping("/active")
    public List<Vehicle> findAllActive() {
        return vehicleService.findAllActive();
    }

    // 根据 ID 查询
    @GetMapping("/{id}")
    public Vehicle findById(@PathVariable Integer id) {
        return vehicleService.findById(id);
    }

    // 新增车辆
    @PostMapping
    public String save(@RequestBody Vehicle vehicle) {
        vehicleService.save(vehicle);
        return "新增成功";
    }

    // 修改车辆
    @PutMapping
    public String update(@RequestBody Vehicle vehicle) {
        vehicleService.update(vehicle);
        return "修改成功";
    }

    // 物理删除车辆
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        vehicleService.deleteById(id);
        return "删除成功";
    }

    // 软删除车辆（失效）
    @DeleteMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Integer id) {
        vehicleService.softDeleteById(id);
        return "车辆已失效";
    }
}