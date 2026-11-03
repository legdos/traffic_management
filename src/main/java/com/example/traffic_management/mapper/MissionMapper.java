package com.example.traffic_management.mapper;

import com.example.traffic_management.entity.Mission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissionMapper {

    int insert(Mission mission);

    Mission findById(Integer id);

    List<Mission> findAll();

    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    Mission findByNeedId(Integer needId);

    /** 按状态取最近 N 条，前端看板分组使用 */
    List<Mission> findByStatus(@Param("status") String status, @Param("limit") int limit);

    /** 取进行中的任务（接单中/装货中/运输中/卸货中） */
    List<Mission> findActive(@Param("limit") int limit);

    /** 取全部进行中的任务，仿真循环一次性加载，避免逐车查询 */
    List<Mission> findAllActive();

    /** 取待接单的任务，按 id 升序，最多 limit 条 */
    List<Mission> findPending(@Param("limit") int limit);

    int countPending();

    /** 查询某辆车当前进行中的任务 */
    Mission findActiveByVehicleId(@Param("vehicleId") Integer vehicleId);

    /**
     * 原子接单：只有仍是「待接单」的任务才能被抢到。
     * 返回 1 表示抢单成功，0 表示已被别的车抢走。
     */
    int claim(@Param("missionId") Integer missionId, @Param("vehicleId") Integer vehicleId);

    /** 更新任务进度（状态、已耗时、总耗时、距离、负责车辆） */
    int updateProgress(Mission mission);

    /** 清空任务，供仿真重置使用 */
    int deleteAll();
}
