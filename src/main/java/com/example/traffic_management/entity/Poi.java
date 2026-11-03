package com.example.traffic_management.entity;

import lombok.Data;

/**
 * 地图点位（poi）。工厂的装卸货点在这里登记，route 表通过 startPoint/endPoint 引用它。
 * 表字段为 POI_type / POI_location / POI_function，需在 Mapper 里显式映射。
 */
@Data
public class Poi {

    private String id;

    private String poiType;

    private String poiLocation;

    private String poiFunction;
}
