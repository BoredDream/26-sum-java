package com.training.system.score.mapper;

import com.training.system.score.entity.StageEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StageEvaluationMapper {
    int insert(StageEvaluation evaluation);

    int update(StageEvaluation evaluation);

    StageEvaluation selectByStageAndTeam(@Param("stageId") Long stageId, @Param("teamId") Long teamId);
}

