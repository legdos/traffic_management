package com.example.traffic_management.entity;

import lombok.Data;
import java.util.Date;



@Data
public class Mission {
    private Integer id;
    private String missionNo;
    private Integer carId;
    private Integer goodsId;
    private Integer quantity;
    private Double totalWeight;
    private Double totalVol;
    private String status;
    private Date createTime;
    private Integer factoryId;
    private Integer routeId;
    private Integer needId;
}