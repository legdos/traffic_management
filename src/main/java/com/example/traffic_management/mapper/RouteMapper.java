package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RouteMapper {

    Route findById(@Param("id") Integer id);

    /** 按起点/终点 POI 查缓存路线 */
    Route findByPoiPair(@Param("startPoint") String startPoint,
                        @Param("endPoint") String endPoint);

    int insert(Route route);
}
