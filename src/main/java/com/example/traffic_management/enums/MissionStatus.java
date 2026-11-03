package com.example.traffic_management.enums;

/**
 * 运输任务（mission）状态常量，与数据库 mission.status 的取值保持一致。
 */
public final class MissionStatus {

    /** 已生成、等待车辆接单 */
    public static final String PENDING = "待接单";
    /** 车辆正在前往装货点 */
    public static final String ACCEPTING = "接单中";
    /** 到达装货点，正在装货 */
    public static final String LOADING = "装货中";
    /** 装货完成，正在运往目的地 */
    public static final String TRANSPORTING = "运输中";
    /** 到达目的地，正在卸货 */
    public static final String UNLOADING = "卸货中";
    /** 卸货完成 */
    public static final String DONE = "已完成";

    /** 车辆"在途"类状态：这些状态下的任务算作进行中 */
    public static final String[] ACTIVE = {ACCEPTING, LOADING, TRANSPORTING, UNLOADING};

    private MissionStatus() {
    }
}
