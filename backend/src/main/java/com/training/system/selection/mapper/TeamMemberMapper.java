package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamMemberEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamMemberMapper {
    @Select("SELECT COUNT(1) FROM team_member WHERE team_id = #{teamId} AND status = 1")
    int countTeamById(@Param("teamId") Long teamId);

    @Select("SELECT student_id FROM team_member WHERE team_id = #{teamId} AND status = 1")
    List<Long> selectStudentIdsByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT member_id AS id, team_id, student_id, CASE WHEN member_role = 1 THEN 'LEADER' ELSE 'MEMBER' END AS member_role, " +
            "work_content, CASE WHEN status = 1 THEN TRUE ELSE FALSE END AS enabled, join_time " +
            "FROM team_member WHERE student_id = #{studentId} AND status = 1 ORDER BY join_time ASC")
    List<TeamMemberEntity> findActiveByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT member_id AS id, team_id, student_id, CASE WHEN member_role = 1 THEN 'LEADER' ELSE 'MEMBER' END AS member_role, " +
            "work_content, CASE WHEN status = 1 THEN TRUE ELSE FALSE END AS enabled, join_time " +
            "FROM team_member WHERE team_id = #{teamId} AND status = 1 ORDER BY join_time ASC")
    List<TeamMemberEntity> findActiveByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT COUNT(1) FROM team_member WHERE team_id = #{teamId} AND status = 1")
    int countActiveByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT member_id AS id, team_id, student_id, CASE WHEN member_role = 1 THEN 'LEADER' ELSE 'MEMBER' END AS member_role, " +
            "work_content, CASE WHEN status = 1 THEN TRUE ELSE FALSE END AS enabled, join_time " +
            "FROM team_member WHERE team_id = #{teamId} AND student_id = #{studentId} LIMIT 1")
    TeamMemberEntity findByTeamIdAndStudentId(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Insert("INSERT INTO team_member(team_id, student_id, member_role, work_content, status, join_time) " +
            "VALUES(#{teamId}, #{studentId}, CASE WHEN #{memberRole} = 'LEADER' THEN 1 ELSE 0 END, #{workContent}, CASE WHEN #{enabled} THEN 1 ELSE 0 END, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "member_id")
    int insert(TeamMemberEntity member);

    @Update("UPDATE team_member SET work_content = #{workContent} WHERE team_id = #{teamId} AND student_id = #{studentId} AND status = 1")
    int updateWorkContent(@Param("teamId") Long teamId, @Param("studentId") Long studentId, @Param("workContent") String workContent);

    @Update("UPDATE team_member SET status = 0 WHERE team_id = #{teamId} AND student_id = #{studentId} AND status = 1")
    int deactivateMember(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Update("UPDATE team_member SET status = 1, join_time = NOW() WHERE team_id = #{teamId} AND student_id = #{studentId}")
    int reactivateMember(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Update("UPDATE team_member SET status = 0 WHERE team_id = #{teamId} AND status = 1")
    int deactivateAllMembers(@Param("teamId") Long teamId);
}
