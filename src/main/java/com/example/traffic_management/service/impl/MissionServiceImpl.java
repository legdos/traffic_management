package com.example.traffic_management.service.impl;

import com.example.traffic_management.entity.Mission;
import com.example.traffic_management.entity.Need;
import com.example.traffic_management.entity.Vehicle;
import com.example.traffic_management.mapper.MissionMapper;
import com.example.traffic_management.mapper.NeedMapper;
import com.example.traffic_management.mapper.VehicleMapper;
import com.example.traffic_management.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MissionServiceImpl implements MissionService {

    @Autowired
    private MissionMapper missionMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private NeedMapper needMapper;

    @Override
    public Mission findById(Integer id) {
        return missionMapper.findById(id);
    }

    @Override
    public List<Mission> findAll() {
        return missionMapper.findAll();
    }

    @Override
    @Transactional
    public Mission createAndAssign(Integer needId) {
        Need need = needMapper.selectById(needId);
        if (need == null) {
            throw new RuntimeException("需求不存在，ID：" + needId);
        }

        List<Vehicle> freeVehicles = vehicleMapper.findFreeVehicles();
        if (freeVehicles.isEmpty()) {
            throw new RuntimeException("无可用车辆");
        }
        Vehicle vehicle = freeVehicles.get(0);

        Mission mission = new Mission();
        mission.setMissionNo("M" + System.currentTimeMillis());
        mission.setCarId(vehicle.getId());
        mission.setGoodsId(need.getId());
        mission.setQuantity(1);
        mission.setTotalWeight(0.0);
        mission.setTotalVol(0.0);
        mission.setStatus("进行中");
        mission.setCreateTime(new Date());
        mission.setFactoryId(need.getFactoryId());
        mission.setRouteId(0);
        mission.setNeedId(needId);

        missionMapper.insert(mission);

        vehicle.setCurrentAction(2);
        vehicleMapper.update(vehicle);

        return mission;
    }

    @Override
    @Transactional
    public List<Mission> batchAssign() {
        List<Need> needs = needMapper.findUnassignedNeeds();
        List<Mission> missions = new java.util.ArrayList<>();
        for (Need need : needs) {
            try {
                missions.add(createAndAssign(need.getId()));
            } catch (RuntimeException e) {
                break;
            }
        }
        return missions;
    }

    @Override
    public void updateStatus(Integer id, String status) {
        missionMapper.updateStatus(id, status);
    }
}