package com.example.traffic_management.entity;

import lombok.Data;

import java.util.Date;

/**
 * 路线实体，对应 route 表。
 * keyPoint 存整条路线的折线点，JSON 数组格式：[[lng,lat],[lng,lat],...]
 */
@Data
public class Route {

    private Integer id;

    private String startPointName;

    private String endPointName;

    /** 起点 poi.id，形如 F864 */
    private String startPoint;

    /** 终点 poi.id */
    private String endPoint;

    /** 道路类型：高速、国道、省道、城市道路 */
    private String roadType;

    /** 路线总长度（km） */
    private Double distance;

    /** 折线点 JSON */
    private String keyPoint;

    /** 预计耗时（分钟） */
    private Integer estimatedTime;

    /** 状态：可用 / 拥堵 / 封闭 */
    private String status;

    private Date createTime;
}
