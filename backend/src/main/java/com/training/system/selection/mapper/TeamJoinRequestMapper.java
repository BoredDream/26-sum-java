package com.training.system.selection.mapper;

import com.training.system.selection.entity.TeamJoinRequestEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamJoinRequestMapper {
    @Select("SELECT * FROM team_join_request WHERE team_id = #{teamId} AND applicant_id = #{applicantId} AND status = 'PENDING' LIMIT 1")
    TeamJoinRequestEntity findPending(@Param("teamId") Long teamId, @Param("applicantId") Long applicantId);

    @Select("SELECT * FROM team_join_request WHERE team_id = #{teamId} AND status = 'PENDING' ORDER BY apply_time ASC")
    List<TeamJoinRequestEntity> findPendingByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM team_join_request WHERE id = #{id}")
    TeamJoinRequestEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO team_join_request(team_id, applicant_id, apply_message, status, apply_time) " +
            "VALUES(#{teamId}, #{applicantId}, #{applyMessage}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeamJoinRequestEntity request);

    @Update("UPDATE team_join_request SET status = #{status}, reviewer_id = #{reviewerId}, review_opinion = #{reviewOpinion}, review_time = NOW() WHERE id = #{id}")
    int audit(TeamJoinRequestEntity request);
}
