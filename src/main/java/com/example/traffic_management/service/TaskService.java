package com.example.traffic_management.service;

import com.example.traffic_management.entity.Task;
import com.example.traffic_management.entity.RevenueResult;
import java.math.BigDecimal;

public interface TaskService {
    Task create(Task task);
    Task findById(Integer id);
    RevenueResult calculateRevenue(Integer id, BigDecimal distanceKm, BigDecimal pricePerTonKm,
                                   BigDecimal fuelCostPerKm, BigDecimal accidentLossRate);
}
