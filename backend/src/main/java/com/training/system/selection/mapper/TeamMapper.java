package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamEntity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TeamMapper {
    @Select("SELECT ti.team_id AS id, ti.team_name, ti.leader_id, ti.team_intro AS introduction, " +
            "CASE WHEN ti.team_status = 2 THEN 'SELECTED' ELSE 'BUILDING' END AS status, " +
            "(SELECT ts.topic_id FROM topic_selection ts WHERE ts.team_id = ti.team_id AND ts.audit_status = 1 ORDER BY ts.audit_time DESC LIMIT 1) AS selected_topic_id, " +
            "6 AS max_size, ti.create_time, ti.update_time " +
            "FROM team_info ti WHERE ti.team_id = #{id} AND ti.is_deleted = 0")
    TeamEntity findById(@Param("id") Long id);

    @Select("SELECT ti.team_id AS id, ti.team_name, ti.leader_id, ti.team_intro AS introduction, " +
            "CASE WHEN ti.team_status = 2 THEN 'SELECTED' ELSE 'BUILDING' END AS status, " +
            "(SELECT ts.topic_id FROM topic_selection ts WHERE ts.team_id = ti.team_id AND ts.audit_status = 1 ORDER BY ts.audit_time DESC LIMIT 1) AS selected_topic_id, " +
            "6 AS max_size, ti.create_time, ti.update_time " +
            "FROM team_info ti WHERE ti.team_name = #{teamName} AND ti.is_deleted = 0 LIMIT 1")
    TeamEntity findByName(@Param("teamName") String teamName);

    @Insert("INSERT INTO team_info(team_name, leader_id, team_intro, team_status, create_time, update_time) " +
            "VALUES(#{teamName}, #{leaderId}, #{introduction}, CASE WHEN #{status} = 'SELECTED' THEN 2 ELSE 0 END, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "team_id")
    int insert(TeamEntity team);

    @Update("UPDATE team_info SET team_status = CASE WHEN #{status} = 'SELECTED' THEN 2 ELSE 0 END, update_time = NOW() WHERE team_id = #{id}")
    int updateSelectionStatus(TeamEntity team);
}
