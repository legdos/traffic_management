package com.example.traffic_management.enums;

import java.util.Random;

/**
 * 车辆状态与马尔科夫状态转换表。
 *
 * <p>转换表（行 = 当前状态，列 = 下一个状态，每行概率和为 1）：
 * <pre>
 *           接单行驶  装货   运货行驶  卸货   停留等待  空闲
 * 接单行驶    0.05   0.90    0.00   0.00   0.05   0.00
 * 装货        0.00   0.02    0.95   0.02   0.01   0.00
 * 运货行驶    0.00   0.00    0.00   0.98   0.02   0.00
 * 卸货        0.00   0.00    0.00   0.00   0.02   0.98
 * 停留等待    0.10   0.05    0.05   0.00   0.20   0.60
 * 空闲        0.90   0.00    0.00   0.00   0.10   0.00
 * </pre>
 *
 * <p>抽样方式：随机取 1~100，按行概率累加划分区间。例如「接单行驶」一行，
 * 1~5 继续接单行驶、6~95 转装货、96~100 转停留等待。
 *
 * <p>注意：枚举声明顺序必须与矩阵行列顺序一致，抽样时靠 {@code ordinal()} 索引。
 */
public enum VehicleState {

    ACCEPTING(2, "接单行驶"),
    LOADING(3, "装货"),
    TRANSPORTING(4, "运货行驶"),
    UNLOADING(5, "卸货"),
    WAITING(6, "停留等待"),
    IDLE(1, "空闲");

    /** 随机数取值范围 1~100 */
    public static final int PROBABILITY_SCALE = 100;

    private static final double[][] TRANSITIONS = {
            /* ACCEPTING    */ {0.05, 0.90, 0.00, 0.00, 0.05, 0.00},
            /* LOADING      */ {0.00, 0.02, 0.95, 0.02, 0.01, 0.00},
            /* TRANSPORTING */ {0.00, 0.00, 0.00, 0.98, 0.02, 0.00},
            /* UNLOADING    */ {0.00, 0.00, 0.00, 0.00, 0.02, 0.98},
            /* WAITING      */ {0.10, 0.05, 0.05, 0.00, 0.20, 0.60},
            /* IDLE         */ {0.90, 0.00, 0.00, 0.00, 0.10, 0.00},
    };

    private final int actionId;
    private final String desc;

    VehicleState(int actionId, String desc) {
        this.actionId = actionId;
        this.desc = desc;
    }

    public int getActionId() {
        return actionId;
    }

    public String getDesc() {
        return desc;
    }

    /** 该状态是否需要在两点之间移动 */
    public boolean isMoving() {
        return this == ACCEPTING || this == TRANSPORTING;
    }

    /** 该状态是否属于"正在执行任务"（停留等待、空闲不算） */
    public boolean isWorking() {
        return this == ACCEPTING || this == LOADING || this == TRANSPORTING || this == UNLOADING;
    }

    /** 数据库 action.id 对应关系：1 空闲、2 接单行驶……7 初始状态（按空闲处理） */
    public static VehicleState fromActionId(Integer actionId) {
        if (actionId != null) {
            for (VehicleState state : values()) {
                if (state.actionId == actionId) {
                    return state;
                }
            }
        }
        return IDLE;
    }

    /**
     * 按状态转换表抽样下一个状态。
     *
     * @param from   当前状态
     * @param random 随机源
     * @return 下一个状态（一定非 null）
     */
    public static VehicleState sampleNextState(VehicleState from, Random random) {
        double[] row = TRANSITIONS[from.ordinal()];
        int r = random.nextInt(PROBABILITY_SCALE) + 1;   // 1 ~ 100
        double accumulated = 0;
        for (int i = 0; i < row.length; i++) {
            if (row[i] <= 0) {
                continue;
            }
            accumulated += row[i];
            if (r <= Math.round(accumulated * PROBABILITY_SCALE)) {
                return values()[i];
            }
        }
        return IDLE;
    }
}
