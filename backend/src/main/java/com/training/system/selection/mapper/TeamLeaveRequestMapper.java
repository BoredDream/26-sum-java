package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamLeaveRequestEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamLeaveRequestMapper {
    @Select("SELECT request_id AS id, team_id, applicant_id, leave_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_leave_request WHERE team_id = #{teamId} AND applicant_id = #{applicantId} AND audit_status = 0 LIMIT 1")
    TeamLeaveRequestEntity findPending(@Param("teamId") Long teamId, @Param("applicantId") Long applicantId);

    @Select("SELECT request_id AS id, team_id, applicant_id, leave_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_leave_request WHERE team_id = #{teamId} AND audit_status = 0 ORDER BY apply_time ASC")
    List<TeamLeaveRequestEntity> findPendingByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT request_id AS id, team_id, applicant_id, leave_message, " +
            "CASE audit_status WHEN 0 THEN 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' END AS status, " +
            "reviewer_id, review_opinion, apply_time, review_time " +
            "FROM team_leave_request WHERE request_id = #{id}")
    TeamLeaveRequestEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO team_leave_request(team_id, applicant_id, leave_message, audit_status, apply_time) " +
            "VALUES(#{teamId}, #{applicantId}, #{leaveMessage}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "request_id")
    int insert(TeamLeaveRequestEntity request);

    @Update("UPDATE team_leave_request SET " +
            "audit_status = CASE WHEN #{status} = 'APPROVED' THEN 1 WHEN #{status} = 'REJECTED' THEN 2 ELSE 0 END, " +
            "reviewer_id = #{reviewerId}, review_opinion = #{reviewOpinion}, review_time = NOW() " +
            "WHERE request_id = #{id}")
    int audit(TeamLeaveRequestEntity request);

    @Update("UPDATE team_leave_request SET audit_status = 2, review_opinion = '团队已解散', review_time = NOW() " +
            "WHERE team_id = #{teamId} AND audit_status = 0")
    int rejectAllPending(@Param("teamId") Long teamId);
}
