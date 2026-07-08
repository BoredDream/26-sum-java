package com.training.system.selection.mapper;

import com.training.system.selection.entity.TopicEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicMapper {
    @Select("<script>" +
            "SELECT topic_id AS id, topic_name AS title, topic_content AS description, " +
            "topic_type AS direction, difficulty, teacher_id, 1 AS min_members, " +
            "student_limit AS max_members, team_limit, " +
            "CASE WHEN status = 2 AND open_status = 1 THEN 'OPEN' ELSE 'CLOSED' END AS status, " +
            "selection_start_time AS selection_start, selection_end_time AS selection_end, create_time " +
            "FROM project_topic WHERE status = 2 AND open_status = 1 AND is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>AND topic_name LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<TopicEntity> findOpenTopics(@Param("keyword") String keyword);

    @Select("SELECT topic_id AS id, topic_name AS title, topic_content AS description, " +
            "topic_type AS direction, difficulty, teacher_id, 1 AS min_members, " +
            "student_limit AS max_members, team_limit, " +
            "CASE WHEN status = 2 AND open_status = 1 THEN 'OPEN' ELSE 'CLOSED' END AS status, " +
            "selection_start_time AS selection_start, selection_end_time AS selection_end, create_time " +
            "FROM project_topic WHERE topic_id = #{id} AND is_deleted = 0")
    TopicEntity findById(@Param("id") Long id);

    @Update("UPDATE project_topic SET open_status = CASE WHEN #{status} = 'OPEN' THEN 1 ELSE open_status END WHERE topic_id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
