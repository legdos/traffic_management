package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Action;
import com.example.traffic_management.mapper.ActionMapper;
import com.example.traffic_management.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActionServiceImpl implements ActionService {

    @Autowired
    private ActionMapper actionMapper;

    @Override
    public Action selectById(Integer id) {
        return actionMapper.selectById(id);
    }

    @Override
    public List<Action> selectAll() {
        return actionMapper.selectAll();
    }

    @Override
    public int insert(Action action) {
        return actionMapper.insert(action);
    }

    @Override
    public int update(Action action) {
        return actionMapper.update(action);
    }

    @Override
    public int deleteById(Integer id) {
        return actionMapper.deleteById(id);
    }
}