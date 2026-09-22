package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Need;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NeedMapper {

    // 查询单个（过滤已删除）
    Need selectById(Integer id);

    // 查询所有未删除的
    List<Need> selectAllNotDeleted();

    // 插入并回写自增ID
    int insert(Need need);

    // 软删除
    int softDeleteById(Integer id);

    // 物理删除（保留备用）
    int deleteById(Integer id);
}