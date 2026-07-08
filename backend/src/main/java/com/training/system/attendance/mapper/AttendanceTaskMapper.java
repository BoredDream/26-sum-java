package com.training.system.attendance.mapper;

import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 签到任务 Mapper
 */
@Mapper
public interface AttendanceTaskMapper {

    @Insert("INSERT INTO attendance_task(task_title, task_type, scope_type, scope_value, start_time, end_time, " +
            "teacher_id, description, status, require_location, location_lng, location_lat, location_radius, location_name, " +
            "create_time, update_time) " +
            "VALUES(#{taskTitle}, #{taskType}, #{scopeType}, #{scopeValue}, #{startTime}, #{endTime}, " +
            "#{teacherId}, #{description}, #{status}, #{requireLocation}, #{locationLng}, #{locationLat}, " +
            "#{locationRadius}, #{locationName}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    int insert(AttendanceTask task);

    @Select("SELECT * FROM attendance_task WHERE task_id = #{taskId}")
    AttendanceTask selectById(Long taskId);

    @Update("UPDATE attendance_task SET status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    int updateStatus(@Param("taskId") Long taskId, @Param("status") Integer status);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("teacherId") Long teacherId,
                          @Param("studentId") Long studentId,
                          @Param("status") Integer status);

    List<AttendanceTaskVO> selectPage(@Param("keyword") String keyword,
                                      @Param("teacherId") Long teacherId,
                                      @Param("studentId") Long studentId,
                                      @Param("status") Integer status,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    AttendanceTaskDetailVO selectDetailById(@Param("taskId") Long taskId);

    @Delete("DELETE FROM attendance_record WHERE task_id = #{taskId}")
    int deleteRecordsByTaskId(@Param("taskId") Long taskId);

    @Delete("DELETE FROM makeup_sign_apply WHERE task_id = #{taskId}")
    int deleteMakeupByTaskId(@Param("taskId") Long taskId);

    @Delete("DELETE FROM attendance_task WHERE task_id = #{taskId}")
    int deleteById(@Param("taskId") Long taskId);
}
