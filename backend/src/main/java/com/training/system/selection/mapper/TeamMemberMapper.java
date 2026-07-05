package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamMemberEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamMemberMapper {
    @Select("SELECT * FROM team_member WHERE student_id = #{studentId} AND active = 1 LIMIT 1")
    TeamMemberEntity findActiveByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM team_member WHERE team_id = #{teamId} AND active = 1 ORDER BY join_time ASC")
    List<TeamMemberEntity> findActiveByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT COUNT(1) FROM team_member WHERE team_id = #{teamId} AND active = 1")
    int countActiveByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM team_member WHERE team_id = #{teamId} AND student_id = #{studentId} LIMIT 1")
    TeamMemberEntity findByTeamIdAndStudentId(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Insert("INSERT INTO team_member(team_id, student_id, member_role, work_content, active, join_time) " +
            "VALUES(#{teamId}, #{studentId}, #{memberRole}, #{workContent}, #{active}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeamMemberEntity member);

    @Update("UPDATE team_member SET work_content = #{workContent} WHERE team_id = #{teamId} AND student_id = #{studentId} AND active = 1")
    int updateWorkContent(@Param("teamId") Long teamId, @Param("studentId") Long studentId, @Param("workContent") String workContent);
}
