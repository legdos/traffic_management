package com.example.traffic_management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 仿真参数，对应 application.yaml 中的 sim.* 配置。
 */
@Component
@ConfigurationProperties(prefix = "sim")
public class SimProperties {

    /** 每轮循环的真实间隔（秒） */
    private int tickSeconds = 5;

    /** 一轮循环代表现实中的分钟数 */
    private int minutesPerTick = 5;

    /** 车辆统一速度（km/h），1km 按 1 分钟计 */
    private double speedKmh = 60;

    /** 装货耗时（分钟） */
    private int loadingMinutes = 60;

    /** 卸货耗时（分钟） */
    private int unloadingMinutes = 60;

    /** 停留等待耗时（分钟） */
    private int waitingMinutes = 10;

    /** 每轮生成一条新运输任务的概率 */
    private double taskProbability = 0.7;

    /** 待接单任务上限，避免无人接单时无限增长 */
    private int maxPendingTasks = 50;

    /** 每轮最多新增多少条任务（实际按"空闲车辆数 - 待接单数"的缺口生成） */
    private int tasksPerTick = 5;

    /**
     * 高德「Web服务」类型 key（不是 JS API key）。
     * 留空时路线由 PolylineUtil 合成；填入后自动切换为真实道路。
     */
    private String amapWebKey = "";

    public int getTickSeconds() {
        return tickSeconds;
    }

    public void setTickSeconds(int tickSeconds) {
        this.tickSeconds = tickSeconds;
    }

    public int getMinutesPerTick() {
        return minutesPerTick;
    }

    public void setMinutesPerTick(int minutesPerTick) {
        this.minutesPerTick = minutesPerTick;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public int getLoadingMinutes() {
        return loadingMinutes;
    }

    public void setLoadingMinutes(int loadingMinutes) {
        this.loadingMinutes = loadingMinutes;
    }

    public int getUnloadingMinutes() {
        return unloadingMinutes;
    }

    public void setUnloadingMinutes(int unloadingMinutes) {
        this.unloadingMinutes = unloadingMinutes;
    }

    public int getWaitingMinutes() {
        return waitingMinutes;
    }

    public void setWaitingMinutes(int waitingMinutes) {
        this.waitingMinutes = waitingMinutes;
    }

    public double getTaskProbability() {
        return taskProbability;
    }

    public void setTaskProbability(double taskProbability) {
        this.taskProbability = taskProbability;
    }

    public int getMaxPendingTasks() {
        return maxPendingTasks;
    }

    public void setMaxPendingTasks(int maxPendingTasks) {
        this.maxPendingTasks = maxPendingTasks;
    }

    public int getTasksPerTick() {
        return tasksPerTick;
    }

    public void setTasksPerTick(int tasksPerTick) {
        this.tasksPerTick = tasksPerTick;
    }

    public String getAmapWebKey() {
        return amapWebKey;
    }

    public void setAmapWebKey(String amapWebKey) {
        this.amapWebKey = amapWebKey;
    }
}
