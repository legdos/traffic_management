package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Poi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PoiMapper {

    /** 不存在才插入，保证 route 的外键总能找到点位 */
    int insertIfAbsent(@Param("id") String id,
                       @Param("poiType") String poiType,
                       @Param("poiLocation") String poiLocation,
                       @Param("poiFunction") String poiFunction);

    Poi findById(@Param("id") String id);
}
