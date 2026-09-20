package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Action;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ActionMapper {
    Action selectById(Integer id);
    List<Action> selectAll();
    int insert(Action action);
    int update(Action action);
    int deleteById(Integer id);
}