package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Task;
import com.example.traffic_management.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.traffic_management.entity.RevenueResult;
import java.math.BigDecimal;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody Task task) {
        return taskService.create(task);
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable Integer id) {
        return taskService.findById(id);
    }

    @GetMapping("/{id}/revenue")
    public RevenueResult revenue(@PathVariable Integer id,
                                 @RequestParam(required = false) BigDecimal distanceKm,
                                 @RequestParam(required = false) BigDecimal pricePerTonKm,
                                 @RequestParam(required = false) BigDecimal fuelCostPerKm,
                                 @RequestParam(required = false) BigDecimal accidentLossRate) {
        return taskService.calculateRevenue(id, distanceKm, pricePerTonKm, fuelCostPerKm, accidentLossRate);
    }
}
