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

    /**
     * 批量导入工厂（来自前端高德 POI 检索），同名同坐标的会跳过。
     *
     * @return 实际新增的条数
     */
    int importFactories(List<Factory> factories);
}
