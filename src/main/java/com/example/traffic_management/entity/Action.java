package com.example.traffic_management.entity;

import lombok.Data;

@Data
public class Action {
    private Integer id;
    private String actionName;
    private String actionType;
    private Integer duration;
    private java.math.BigDecimal accidentRate;
}