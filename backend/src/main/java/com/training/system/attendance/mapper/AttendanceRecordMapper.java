package com.training.system.attendance.mapper;

import com.training.system.attendance.entity.AttendanceRecord;
import com.training.system.attendance.vo.AttendanceRecordVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 考勤记录 Mapper
 */
@Mapper
public interface AttendanceRecordMapper {

    @Insert("INSERT INTO attendance_record(task_id, student_id, sign_status, is_makeup, create_time, update_time) " +
            "VALUES(#{taskId}, #{studentId}, 0, 0, NOW(), NOW())")
    int insertInitRecord(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Select("SELECT * FROM attendance_record WHERE record_id = #{recordId}")
    AttendanceRecord selectById(Long recordId);

    @Select("SELECT * FROM attendance_record WHERE task_id = #{taskId} AND student_id = #{studentId}")
    AttendanceRecord selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Update("UPDATE attendance_record SET sign_time = #{signTime}, sign_status = #{signStatus}, " +
            "remark = #{remark}, is_makeup = #{isMakeup}, update_time = NOW() " +
            "WHERE record_id = #{recordId}")
    int update(AttendanceRecord record);

    long countByCondition(@Param("taskId") Long taskId,
                          @Param("studentId") Long studentId,
                          @Param("signStatus") Integer signStatus,
                          @Param("keyword") String keyword,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate,
                          @Param("teacherId") Long teacherId);

    List<AttendanceRecordVO> selectPage(@Param("taskId") Long taskId,
                                        @Param("studentId") Long studentId,
                                        @Param("signStatus") Integer signStatus,
                                        @Param("keyword") String keyword,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("teacherId") Long teacherId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    List<AttendanceRecordVO> selectListForExport(@Param("taskId") Long taskId,
                                                 @Param("studentId") Long studentId,
                                                 @Param("signStatus") Integer signStatus,
                                                 @Param("keyword") String keyword,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("teacherId") Long teacherId);

    List<AttendanceRecordVO> selectListForStatistics(@Param("taskId") Long taskId,
                                                       @Param("studentId") Long studentId,
                                                       @Param("className") String className,
                                                       @Param("teamId") Long teamId,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("teacherId") Long teacherId);
}
