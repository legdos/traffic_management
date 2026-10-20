package com.example.traffic_management.enums;

public enum VehicleState {
    IDLE(1, "空闲", 0.2, 10),
    ACCEPTING(2, "接单行驶", 0.5, 15),
    LOADING(3, "装货", 0.4, 20),
    TRANSPORTING(4, "运货行驶", 0.3, 30),
    UNLOADING(5, "卸货", 0.4, 20),
    WAITING(6, "停留等待", 0.3, 10),
    INIT(7, "初始状态", 0.0, 0);

    private final int actionId;
    private final String desc;
    private final double transitionProb; // 每次循环触发转移的概率
    private final int baseDuration;      // 进入该状态的基础时长(分钟)

    VehicleState(int actionId, String desc, double transitionProb, int baseDuration) {
        this.actionId = actionId;
        this.desc = desc;
        this.transitionProb = transitionProb;
        this.baseDuration = baseDuration;
    }

    public int getActionId() { return actionId; }
    public String getDesc() { return desc; }
    public double getTransitionProb() { return transitionProb; }
    public int getBaseDuration() { return baseDuration; }

    // 根据ID获取枚举
    public static VehicleState fromActionId(int actionId) {
        for (VehicleState state : values()) {
            if (state.getActionId() == actionId) return state;
        }
        return INIT;
    }

    // 马尔科夫链：简单状态流转规则
    public static VehicleState getNextState(VehicleState current) {
        switch (current) {
            case IDLE: return ACCEPTING;
            case ACCEPTING: return LOADING;
            case LOADING: return TRANSPORTING;
            case TRANSPORTING: return UNLOADING;
            case UNLOADING: return WAITING;
            case WAITING: return IDLE;
            default: return IDLE;
        }
    }
}