package com.example.traffic_management.entity;

import lombok.Data;

@Data
public class Need {

    // 需求ID (主键)
    private Integer id;

    // 需求内容
    private String needContent;

    // 关联工厂ID
    private Integer factoryId;

    private Integer isDeleted;
}