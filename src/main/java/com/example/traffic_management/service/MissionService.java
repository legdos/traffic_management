package com.example.traffic_management.service;

import com.example.traffic_management.entity.Mission;
import java.util.List;

public interface MissionService {
    Mission findById(Integer id);
    List<Mission> findAll();
    Mission createAndAssign(Integer needId);
    List<Mission> batchAssign();
    void updateStatus(Integer id, String status);
}
