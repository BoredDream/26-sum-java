package com.training.system.selection.mapper;

import com.training.system.selection.entity.DevelopmentLogEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DevelopmentLogMapper {
    @Select("SELECT log_id AS id, team_id, student_id, log_title AS title, log_date, work_content, completion_status, " +
            "problem_description, next_plan, teacher_feedback, feedback_teacher_id, create_time, update_time " +
            "FROM development_log WHERE student_id = #{studentId} AND log_date = #{logDate} AND is_deleted = 0 LIMIT 1")
    DevelopmentLogEntity findByStudentAndDate(@Param("studentId") Long studentId, @Param("logDate") LocalDate logDate);

    @Insert("INSERT INTO development_log(team_id, student_id, log_title, log_date, work_content, completion_status, problem_description, next_plan, create_time, update_time) " +
            "VALUES(#{teamId}, #{studentId}, #{title}, #{logDate}, #{workContent}, #{completionStatus}, #{problemDescription}, #{nextPlan}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "log_id")
    int insert(DevelopmentLogEntity log);

    @Select("SELECT log_id AS id, team_id, student_id, log_title AS title, log_date, work_content, completion_status, " +
            "problem_description, next_plan, teacher_feedback, feedback_teacher_id, create_time, update_time " +
            "FROM development_log WHERE log_id = #{id} AND is_deleted = 0")
    DevelopmentLogEntity findById(@Param("id") Long id);

    @Select("SELECT log_id AS id, team_id, student_id, log_title AS title, log_date, work_content, completion_status, " +
            "problem_description, next_plan, teacher_feedback, feedback_teacher_id, create_time, update_time " +
            "FROM development_log WHERE team_id = #{teamId} AND is_deleted = 0 ORDER BY log_date DESC, create_time DESC")
    List<DevelopmentLogEntity> findByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT d.log_id AS id, d.team_id, d.student_id, d.log_title AS title, d.log_date, d.work_content, d.completion_status, " +
            "d.problem_description, d.next_plan, d.teacher_feedback, d.feedback_teacher_id, d.create_time, d.update_time " +
            "FROM development_log d INNER JOIN topic_selection ts ON ts.team_id = d.team_id AND ts.audit_status = 1 " +
            "INNER JOIN project_topic tp ON tp.topic_id = ts.topic_id " +
            "WHERE tp.teacher_id = #{teacherId} AND d.is_deleted = 0 ORDER BY d.log_date DESC, d.create_time DESC")
    List<DevelopmentLogEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT log_id AS id, team_id, student_id, log_title AS title, log_date, work_content, completion_status, " +
            "problem_description, next_plan, teacher_feedback, feedback_teacher_id, create_time, update_time " +
            "FROM development_log WHERE is_deleted = 0 ORDER BY log_date DESC, create_time DESC")
    List<DevelopmentLogEntity> findAll();

    @Update("UPDATE development_log SET teacher_feedback = #{teacherFeedback}, feedback_teacher_id = #{feedbackTeacherId}, update_time = NOW() WHERE log_id = #{id}")
    int updateFeedback(DevelopmentLogEntity log);
}
