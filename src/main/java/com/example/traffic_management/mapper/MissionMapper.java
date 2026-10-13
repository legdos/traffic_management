package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Mission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MissionMapper {
    int insert(Mission mission);
    Mission findById(Integer id);
    List<Mission> findAll();
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    Mission findByNeedId(Integer needId);
}