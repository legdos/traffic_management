package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Need;
import com.example.traffic_management.mapper.NeedMapper;
import com.example.traffic_management.service.NeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class NeedServiceImpl implements NeedService {

    @Autowired
    private NeedMapper needMapper;

    @Override
    public Need findById(Integer id) {
        return needMapper.selectById(id);
    }

    @Override
    public List<Need> findAllActive() {
        return needMapper.selectAllNotDeleted();
    }

    @Override
    public void save(Need need) {
        needMapper.insert(need);
    }

    @Override
    public void softDeleteById(Integer id) {
        needMapper.softDeleteById(id);
    }

    @Override
    public void deleteById(Integer id) {
        needMapper.deleteById(id);
    }
}