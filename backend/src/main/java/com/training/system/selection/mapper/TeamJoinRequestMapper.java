package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamJoinRequestEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamJoinRequestMapper {
    @Select("SELECT request_id AS id, team_id, applicant_id, apply_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' ELSE 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_join_request WHERE team_id = #{teamId} AND applicant_id = #{applicantId} AND audit_status = 0 LIMIT 1")
    TeamJoinRequestEntity findPending(@Param("teamId") Long teamId, @Param("applicantId") Long applicantId);

    @Select("SELECT request_id AS id, team_id, applicant_id, apply_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' ELSE 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_join_request WHERE team_id = #{teamId} AND audit_status = 0 ORDER BY apply_time ASC")
    List<TeamJoinRequestEntity> findPendingByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT request_id AS id, team_id, applicant_id, apply_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' ELSE 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_join_request WHERE request_id = #{id}")
    TeamJoinRequestEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO team_join_request(team_id, applicant_id, apply_message, audit_status, apply_time) " +
            "VALUES(#{teamId}, #{applicantId}, #{applyMessage}, CASE WHEN #{status} = 'APPROVED' THEN 1 WHEN #{status} = 'REJECTED' THEN 2 ELSE 0 END, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "request_id")
    int insert(TeamJoinRequestEntity request);

    @Update("UPDATE team_join_request SET audit_status = CASE WHEN #{status} = 'APPROVED' THEN 1 WHEN #{status} = 'REJECTED' THEN 2 ELSE 0 END, " +
            "reviewer_id = #{reviewerId}, review_opinion = #{reviewOpinion}, review_time = NOW() WHERE request_id = #{id}")
    int audit(TeamJoinRequestEntity request);
}
