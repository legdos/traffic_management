package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Factory;
import com.example.traffic_management.entity.Need;
import com.example.traffic_management.mapper.FactoryMapper;
import com.example.traffic_management.mapper.NeedMapper;
import com.example.traffic_management.service.FactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FactoryServiceImpl implements FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;

    @Autowired
    private NeedMapper needMapper;

    @Override
    public Factory findById(Integer id) {
        return factoryMapper.selectById(id);
    }

    @Override
    public List<Factory> findAll() {
        return factoryMapper.selectAll();
    }

    @Override
    public void save(Factory factory) {
        factoryMapper.insert(factory);
    }

    @Override
    public void update(Factory factory) {
        factoryMapper.update(factory);
    }

    @Override
    public void deleteById(Integer id) {
        factoryMapper.deleteById(id);
    }

    @Override
    public Integer generateNeed(Integer factoryId, String materialType) {
        // 1. 校验工厂是否存在
        Factory factory = factoryMapper.selectById(factoryId);
        if (factory == null) {
            throw new RuntimeException("工厂不存在，无法生成需求");
        }

        // 2. 真实向 need 表插入数据
        Need need = new Need();
        need.setNeedContent(materialType); // 如 "木头"、"铁"
        need.setFactoryId(factoryId);      // 记录需求归属的工厂
        needMapper.insert(need);           // 真正执行 INSERT

        // 3. 获取刚插入的自增ID，回写工厂表的 need_id
        Integer realNeedId = need.getId();
        factoryMapper.updateNeedId(factoryId, realNeedId);

        // 4. 返回需求ID（便于前端或后续业务追踪）
        return realNeedId;
    }
}