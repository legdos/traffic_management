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

    private String currentAction;
    
    private Integer isDeleted;
}