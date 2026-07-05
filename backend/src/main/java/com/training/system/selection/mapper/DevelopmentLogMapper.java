package com.training.system.selection.mapper;

import com.training.system.selection.entity.DevelopmentLogEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DevelopmentLogMapper {
    @Select("SELECT * FROM development_log WHERE student_id = #{studentId} AND log_date = #{logDate} LIMIT 1")
    DevelopmentLogEntity findByStudentAndDate(@Param("studentId") Long studentId, @Param("logDate") LocalDate logDate);

    @Insert("INSERT INTO development_log(team_id, student_id, title, log_date, work_content, completion_status, problem_description, next_plan, create_time, update_time) " +
            "VALUES(#{teamId}, #{studentId}, #{title}, #{logDate}, #{workContent}, #{completionStatus}, #{problemDescription}, #{nextPlan}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DevelopmentLogEntity log);

    @Select("SELECT * FROM development_log WHERE id = #{id}")
    DevelopmentLogEntity findById(@Param("id") Long id);

    @Select("SELECT * FROM development_log WHERE team_id = #{teamId} ORDER BY log_date DESC, create_time DESC")
    List<DevelopmentLogEntity> findByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT d.* FROM development_log d INNER JOIN team tm ON tm.id = d.team_id " +
            "INNER JOIN topic tp ON tp.id = tm.selected_topic_id " +
            "WHERE tp.teacher_id = #{teacherId} ORDER BY d.log_date DESC, d.create_time DESC")
    List<DevelopmentLogEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM development_log ORDER BY log_date DESC, create_time DESC")
    List<DevelopmentLogEntity> findAll();

    @Update("UPDATE development_log SET teacher_feedback = #{teacherFeedback}, feedback_teacher_id = #{feedbackTeacherId}, update_time = NOW() WHERE id = #{id}")
    int updateFeedback(DevelopmentLogEntity log);
}
