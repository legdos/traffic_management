package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Factory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FactoryMapper {
    // 根据主键查询工厂
    Factory selectById(Integer id);

    // 查询所有工厂
    List<Factory> selectAll();

    // 插入工厂
    int insert(Factory factory);

    // 更新工厂
    int update(Factory factory);

    // 删除工厂（物理删除）
    int deleteById(Integer id);

    // 生成需求：回写工厂关联的 need_id
    int updateNeedId(Integer factoryId, Integer needId);
}