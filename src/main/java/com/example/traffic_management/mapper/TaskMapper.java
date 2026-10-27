package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {
    Task selectById(Integer id);
    Task selectActiveByNeedId(Integer needId);
    Task selectActiveByVehicleId(Integer vehicleId);
    int insert(Task task);
}
