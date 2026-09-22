package com.example.traffic_management.service;

import com.example.traffic_management.entity.Need;
import java.util.List;

public interface NeedService {

    // 查询单个（过滤已删除）
    Need findById(Integer id);

    // 查询所有未删除的
    List<Need> findAllActive();

    // 新增需求
    void save(Need need);

    // 软删除
    void softDeleteById(Integer id);

    // 物理删除（保留备用）
    void deleteById(Integer id);
}