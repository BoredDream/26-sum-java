package com.training.system.selection.mapper;

import com.training.system.selection.entity.TopicSelectionEntity;
import com.training.system.selection.vo.SelectionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicSelectionMapper {
    @Select("SELECT selection_id AS id, team_id, topic_id, selection_reason, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'WITHDRAWN' END AS status, " +
            "audit_teacher_id, audit_opinion, apply_time, audit_time " +
            "FROM topic_selection WHERE team_id = #{teamId} AND audit_status = 0 LIMIT 1")
    TopicSelectionEntity findPendingByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT selection_id AS id, team_id, topic_id, selection_reason, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'WITHDRAWN' END AS status, " +
            "audit_teacher_id, audit_opinion, apply_time, audit_time " +
            "FROM topic_selection WHERE selection_id = #{id}")
    TopicSelectionEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO topic_selection(team_id, topic_id, selection_reason, audit_status, apply_time) " +
            "VALUES(#{teamId}, #{topicId}, #{selectionReason}, CASE WHEN #{status} = 'APPROVED' THEN 1 WHEN #{status} = 'REJECTED' THEN 2 ELSE 0 END, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "selection_id")
    int insert(TopicSelectionEntity selection);

    @Update("UPDATE topic_selection SET audit_status = CASE WHEN #{status} = 'APPROVED' THEN 1 WHEN #{status} = 'REJECTED' THEN 2 ELSE 0 END, " +
            "audit_teacher_id = #{auditTeacherId}, audit_opinion = #{auditOpinion}, audit_time = NOW() WHERE selection_id = #{id}")
    int audit(TopicSelectionEntity selection);

    @Update("UPDATE topic_selection SET audit_status = 2, audit_opinion = '团队已解散', audit_time = NOW() " +
            "WHERE team_id = #{teamId} AND audit_status = 0")
    int cancelPending(@Param("teamId") Long teamId);

    @Select("SELECT s.selection_id, s.team_id, tm.team_name, s.topic_id, tp.topic_name AS topic_title, " +
            "s.selection_reason, CASE s.audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'WITHDRAWN' END AS status, " +
            "s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team_info tm ON tm.team_id = s.team_id " +
            "LEFT JOIN project_topic tp ON tp.topic_id = s.topic_id " +
            "WHERE s.team_id = #{teamId} ORDER BY s.apply_time DESC")
    List<SelectionVO> findViewByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT s.selection_id, s.team_id, tm.team_name, s.topic_id, tp.topic_name AS topic_title, " +
            "s.selection_reason, CASE s.audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'WITHDRAWN' END AS status, " +
            "s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team_info tm ON tm.team_id = s.team_id " +
            "INNER JOIN project_topic tp ON tp.topic_id = s.topic_id " +
            "WHERE s.audit_status = 0 AND tp.teacher_id = #{teacherId} " +
            "ORDER BY s.apply_time ASC")
    List<SelectionVO> findPendingByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT s.selection_id, s.team_id, tm.team_name, s.topic_id, tp.topic_name AS topic_title, " +
            "s.selection_reason, CASE s.audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'WITHDRAWN' END AS status, " +
            "s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team_info tm ON tm.team_id = s.team_id " +
            "LEFT JOIN project_topic tp ON tp.topic_id = s.topic_id " +
            "WHERE s.audit_status = 0 ORDER BY s.apply_time ASC")
    List<SelectionVO> findAllPending();
}
