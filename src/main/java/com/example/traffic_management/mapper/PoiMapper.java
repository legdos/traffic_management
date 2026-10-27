package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Poi;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PoiMapper {
    Poi selectActiveById(Integer id);
    List<Poi> selectAllActive();
    int insert(Poi poi);
    int countRouteReferences(Integer id);
    int softDeleteById(Integer id);
}
