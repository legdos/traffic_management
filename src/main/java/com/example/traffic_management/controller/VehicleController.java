package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.Instant;
import java.util.Date;
import org.springframework.http.HttpStatus;
import com.example.traffic_management.entity.VehiclePositionRequest;
import com.example.traffic_management.entity.VehicleStateRequest;
import org.springframework.web.server.ResponseStatusException;

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

    @PutMapping("/{id}/position")
    public Vehicle updatePosition(@PathVariable Integer id, @RequestBody VehiclePositionRequest request) {
        if (request == null || request.getVersion() == null || request.getLatitude() == null || request.getLongitude() == null
                || request.getLatitude() < -90 || request.getLatitude() > 90 || request.getLongitude() < -180 || request.getLongitude() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "version、经纬度不能为空且必须在有效范围内");
        }
        Date observedAt;
        try { observedAt = request.getObservedAt() == null ? new Date() : Date.from(Instant.parse(request.getObservedAt())); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "observedAt 必须是 ISO-8601 时间"); }
        if (vehicleService.updatePosition(id, request.getVersion(), request.getLatitude(), request.getLongitude(), observedAt) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "车辆版本已变化或车辆已失效，请先查询最新版本");
        }
        return vehicleService.findById(id);
    }

    @PutMapping("/{id}/state")
    public Vehicle updateState(@PathVariable Integer id, @RequestBody VehicleStateRequest request) {
        if (request == null || request.getVersion() == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "version 和 status 不能为空");
        }
        Vehicle current = vehicleService.findById(id);
        if (!allowed(current.getStatus(), request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "不允许的车辆状态流转");
        }
        if (vehicleService.updateState(id, request.getVersion(), request.getStatus()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "车辆版本已变化或车辆已失效，请先查询最新版本");
        }
        return vehicleService.findById(id);
    }

    private boolean allowed(String from, String to) {
        if (from == null || to == null) return false;
        if (from.equalsIgnoreCase(to)) return true;
        return switch (from.toUpperCase()) {
            case "ASSIGNED" -> to.equalsIgnoreCase("LOADING");
            case "LOADING" -> to.equalsIgnoreCase("TRANSPORTING");
            case "TRANSPORTING" -> to.equalsIgnoreCase("UNLOADING");
            case "UNLOADING" -> to.equalsIgnoreCase("AVAILABLE");
            case "AVAILABLE" -> to.equalsIgnoreCase("RESTING") || to.equalsIgnoreCase("MAINTENANCE");
            case "RESTING", "MAINTENANCE" -> to.equalsIgnoreCase("AVAILABLE");
            default -> false;
        };
    }
}
