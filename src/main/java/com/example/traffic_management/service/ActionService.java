package com.example.traffic_management.service;

import com.example.traffic_management.entity.Action;
import java.util.List;

public interface ActionService {
    Action selectById(Integer id);
    List<Action> selectAll();
    int insert(Action action);
    int update(Action action);
    int deleteById(Integer id);
}