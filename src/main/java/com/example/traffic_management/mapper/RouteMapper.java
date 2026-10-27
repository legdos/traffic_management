package com.example.traffic_management.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.math.BigDecimal;

@Mapper
public interface RouteMapper {
    BigDecimal selectDistanceById(Integer id);
}
