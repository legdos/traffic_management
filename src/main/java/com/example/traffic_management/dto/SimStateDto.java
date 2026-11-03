package com.example.traffic_management.dto;

import com.example.traffic_management.entity.Mission;
import com.example.traffic_management.entity.Vehicle;

import java.util.List;

/**
 * /sim/state 的返回体：一次请求拿到前端渲染所需的全部动态数据。
 */
public class SimStateDto {

    /** 仿真是否在运行 */
    private boolean running;

    /** 已执行的循环轮次 */
    private int tick;

    /** 仿真世界已经过去的分钟数 */
    private int simMinutes;

    /** 每轮的真实间隔（秒），前端据此做位置插值动画 */
    private int tickSeconds;

    private List<Vehicle> vehicles;

    /** 待接单任务（最近 10 条） */
    private List<Mission> pendingMissions;

    /** 进行中任务（最近 10 条） */
    private List<Mission> activeMissions;

    /** 已完成任务（最近 10 条） */
    private List<Mission> doneMissions;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public int getSimMinutes() {
        return simMinutes;
    }

    public void setSimMinutes(int simMinutes) {
        this.simMinutes = simMinutes;
    }

    public int getTickSeconds() {
        return tickSeconds;
    }

    public void setTickSeconds(int tickSeconds) {
        this.tickSeconds = tickSeconds;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Mission> getPendingMissions() {
        return pendingMissions;
    }

    public void setPendingMissions(List<Mission> pendingMissions) {
        this.pendingMissions = pendingMissions;
    }

    public List<Mission> getActiveMissions() {
        return activeMissions;
    }

    public void setActiveMissions(List<Mission> activeMissions) {
        this.activeMissions = activeMissions;
    }

    public List<Mission> getDoneMissions() {
        return doneMissions;
    }

    public void setDoneMissions(List<Mission> doneMissions) {
        this.doneMissions = doneMissions;
    }
}
