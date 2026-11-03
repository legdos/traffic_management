package com.example.traffic_management.entity;

import lombok.Data;
import java.util.Date;



@Data
public class Mission {
    private Integer id;
    private String missionNo;
    private Integer carId;
    private Integer goodsId;
    /** 货物类型（仿真生成，自由文本） */
    private String goodsType;
    private Integer quantity;
    private Double totalWeight;
    private Double totalVol;
    /** 起点到终点的距离（km） */
    private Double distanceKm;
    /** 任务总耗时（分钟） */
    private Integer totalMinutes;
    /** 任务已耗时（分钟） */
    private Integer elapsedMinutes;
    private String status;
    private Date createTime;
    /** 起点工厂 */
    private Integer factoryId;
    /** 终点工厂 */
    private Integer destFactoryId;
    private Integer routeId;
    private Integer needId;
}