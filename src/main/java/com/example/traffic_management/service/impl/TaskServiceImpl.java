package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Need;
import com.example.traffic_management.entity.Task;
import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.NeedMapper;
import com.example.traffic_management.mapper.TaskMapper;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.mapper.RouteMapper;
import com.example.traffic_management.entity.RevenueResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.example.traffic_management.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;
    private final NeedMapper needMapper;
    private final VehicleMapper vehicleMapper;
    private final RouteMapper routeMapper;

    public TaskServiceImpl(TaskMapper taskMapper, NeedMapper needMapper, VehicleMapper vehicleMapper, RouteMapper routeMapper) {
        this.taskMapper = taskMapper;
        this.needMapper = needMapper;
        this.vehicleMapper = vehicleMapper;
        this.routeMapper = routeMapper;
    }

    @Override
    @Transactional
    public Task create(Task task) {
        if (task == null || task.getNeedId() == null || task.getVehicleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "needId 和 vehicleId 不能为空");
        }
        Need need = needMapper.selectById(task.getNeedId());
        if (need == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在或已失效");
        }
        Vehicle vehicle = vehicleMapper.selectById(task.getVehicleId());
        if (vehicle == null || vehicle.getIsDeleted() != null && vehicle.getIsDeleted() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "车辆不存在或已失效");
        }
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "车辆当前状态不能指派任务");
        }
        if (taskMapper.selectActiveByNeedId(task.getNeedId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该需求已有进行中的任务");
        }
        if (taskMapper.selectActiveByVehicleId(task.getVehicleId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该车辆已有进行中的任务");
        }
        if (task.getTaskName() == null || task.getTaskName().isBlank()) {
            task.setTaskName("运输任务-需求" + task.getNeedId());
        }
        taskMapper.insert(task);
        int needChanged = needMapper.markAssigned(task.getNeedId());
        int vehicleChanged = vehicleMapper.markAssigned(task.getVehicleId());
        if (needChanged != 1 || vehicleChanged != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "需求或车辆状态已发生变化，请重试");
        }
        task.setStatus("ASSIGNED");
        return taskMapper.selectById(task.getId());
    }

    @Override
    public Task findById(Integer id) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在");
        return task;
    }

    @Override
    public RevenueResult calculateRevenue(Integer id, BigDecimal distanceKm, BigDecimal pricePerTonKm,
                                          BigDecimal fuelCostPerKm, BigDecimal accidentLossRate) {
        Task task = findById(id);
        Need need = needMapper.selectById(task.getNeedId());
        if (need == null || need.getCargoTotalWeight() == null || need.getCargoTotalWeight().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "任务没有有效的货物重量");
        }
        if (distanceKm == null && task.getRouteId() != null) distanceKm = routeMapper.selectDistanceById(task.getRouteId());
        if (distanceKm == null || distanceKm.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请提供大于 0 的 distanceKm，或为任务设置有效路线");
        }
        if (pricePerTonKm == null) pricePerTonKm = new BigDecimal("10.00");
        if (fuelCostPerKm == null) fuelCostPerKm = new BigDecimal("2.00");
        if (accidentLossRate == null) accidentLossRate = BigDecimal.ZERO;
        if (pricePerTonKm.signum() < 0 || fuelCostPerKm.signum() < 0 || accidentLossRate.compareTo(BigDecimal.ZERO) < 0 || accidentLossRate.compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "单价、成本不能为负，事故损失率必须在 0 到 1 之间");
        }
        BigDecimal revenue = need.getCargoTotalWeight().multiply(distanceKm).multiply(pricePerTonKm);
        BigDecimal fuel = distanceKm.multiply(fuelCostPerKm);
        BigDecimal loss = revenue.multiply(accidentLossRate);
        RevenueResult result = new RevenueResult();
        result.setTaskId(id); result.setCargoWeightT(need.getCargoTotalWeight()); result.setDistanceKm(distanceKm);
        result.setPricePerTonKm(pricePerTonKm); result.setFuelCostPerKm(fuelCostPerKm); result.setAccidentLossRate(accidentLossRate);
        result.setTransportRevenue(money(revenue)); result.setFuelCost(money(fuel)); result.setAccidentLoss(money(loss));
        result.setNetRevenue(money(revenue.subtract(fuel).subtract(loss))); result.setCurrency("CNY");
        return result;
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
}
