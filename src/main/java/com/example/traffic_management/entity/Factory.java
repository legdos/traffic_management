package com.example.traffic_management.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Factory {
    private Integer id;

    private String factoryName;

    private String address;

    private String phone;

    private Date createTime;

    private Integer needId;

    private Double factoryLatitude;

    private Double factoryLongitude;
}