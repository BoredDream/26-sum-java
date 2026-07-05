package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamEntity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TeamMapper {
    @Select("SELECT * FROM team WHERE id = #{id}")
    TeamEntity findById(@Param("id") Long id);

    @Select("SELECT * FROM team WHERE team_name = #{teamName} LIMIT 1")
    TeamEntity findByName(@Param("teamName") String teamName);

    @Insert("INSERT INTO team(team_name, leader_id, introduction, status, selected_topic_id, max_size, create_time, update_time) " +
            "VALUES(#{teamName}, #{leaderId}, #{introduction}, #{status}, #{selectedTopicId}, #{maxSize}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeamEntity team);

    @Update("UPDATE team SET status = #{status}, selected_topic_id = #{selectedTopicId}, update_time = NOW() WHERE id = #{id}")
    int updateSelectionStatus(TeamEntity team);
}
