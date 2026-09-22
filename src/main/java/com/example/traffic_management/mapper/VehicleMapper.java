package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface VehicleMapper {
    // 根据主键查询车辆
    Vehicle selectById(Integer id);

    // 查询所有车辆
    List<Vehicle> selectAll();

    // 插入车辆
    int insert(Vehicle vehicle);

    // 更新车辆
    int update(Vehicle vehicle);

    // 删除车辆
    int deleteById(Integer id);

    int softDeleteById(Integer id);          // 软删除：将 isDeleted 设为 1

    List<Vehicle> selectAllNotDeleted();     // 查询未删除的车辆
}