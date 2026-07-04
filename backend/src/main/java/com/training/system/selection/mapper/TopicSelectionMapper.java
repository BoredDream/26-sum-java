package com.training.system.selection.mapper;

import com.training.system.selection.entity.TopicSelectionEntity;
import com.training.system.selection.vo.SelectionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicSelectionMapper {
    @Select("SELECT * FROM topic_selection WHERE team_id = #{teamId} AND status = 'PENDING' LIMIT 1")
    TopicSelectionEntity findPendingByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM topic_selection WHERE id = #{id}")
    TopicSelectionEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO topic_selection(team_id, topic_id, selection_reason, status, apply_time) " +
            "VALUES(#{teamId}, #{topicId}, #{selectionReason}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TopicSelectionEntity selection);

    @Update("UPDATE topic_selection SET status = #{status}, audit_teacher_id = #{auditTeacherId}, audit_opinion = #{auditOpinion}, audit_time = NOW() WHERE id = #{id}")
    int audit(TopicSelectionEntity selection);

    @Select("SELECT s.id AS selection_id, s.team_id, tm.team_name, s.topic_id, tp.title AS topic_title, " +
            "s.selection_reason, s.status, s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team tm ON tm.id = s.team_id " +
            "LEFT JOIN topic tp ON tp.id = s.topic_id " +
            "WHERE s.team_id = #{teamId} ORDER BY s.apply_time DESC")
    List<SelectionVO> findViewByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT s.id AS selection_id, s.team_id, tm.team_name, s.topic_id, tp.title AS topic_title, " +
            "s.selection_reason, s.status, s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team tm ON tm.id = s.team_id " +
            "INNER JOIN topic tp ON tp.id = s.topic_id " +
            "WHERE s.status = 'PENDING' AND tp.teacher_id = #{teacherId} " +
            "ORDER BY s.apply_time ASC")
    List<SelectionVO> findPendingByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT s.id AS selection_id, s.team_id, tm.team_name, s.topic_id, tp.title AS topic_title, " +
            "s.selection_reason, s.status, s.audit_teacher_id, s.audit_opinion, s.apply_time, s.audit_time " +
            "FROM topic_selection s " +
            "LEFT JOIN team tm ON tm.id = s.team_id " +
            "LEFT JOIN topic tp ON tp.id = s.topic_id " +
            "WHERE s.status = 'PENDING' ORDER BY s.apply_time ASC")
    List<SelectionVO> findAllPending();
}
