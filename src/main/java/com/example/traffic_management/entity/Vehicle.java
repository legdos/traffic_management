package com.example.traffic_management.entity;

import lombok.Data;

@Data
public class Vehicle {

    private Integer id;

    private String bandName;

    private String licenseNumber;

    private String typeCode;

    private Double currentLatitude;

    private Double currentLongitude;

    private int currentAction;
    
    private Integer isDeleted;

    private Integer remainingTime;    // 当前状态剩余时间（分钟）

    private Double distanceToPickup;  // 到取货点的距离（km）

    private Double transportDistance; // 运输距离（km）
}