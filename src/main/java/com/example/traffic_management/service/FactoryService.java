package com.example.traffic_management.service;

import com.example.traffic_management.entity.Factory;
import java.util.List;

public interface FactoryService {
    Factory findById(Integer id);

    List<Factory> findAll();

    void save(Factory factory);

    void update(Factory factory);

    void deleteById(Integer id);        // 保留：物理删除

    Integer generateNeed(Integer factoryId, String materialType); // 生成需求并回写need_id
}