package com.training.system.score.service;

import com.training.system.common.PageResult;
import com.training.system.score.dto.*;
import com.training.system.score.vo.ProgressVO;
import com.training.system.score.vo.ScoreVO;
import com.training.system.score.vo.StageTaskVO;
import com.training.system.score.vo.StudentScoreVO;

import java.util.List;

public interface ScoreService {
    void createStageTask(StageTaskCreateDTO dto, Long userId, String role, Long relatedId);

    PageResult<StageTaskVO> queryStageTaskPage(ScorePageQueryDTO dto, String role, Long relatedId);

    void evaluateStage(StageEvaluationDTO dto, Long userId, String role, Long relatedId);

    List<ProgressVO> queryProgress(Long teamId, String role, Long relatedId);

    void recordContribution(ContributionDTO dto, Long userId, String role, Long relatedId);

    void saveTeamScore(ScoreSaveDTO dto, Long userId, String role, Long relatedId);

    PageResult<ScoreVO> queryScorePage(ScorePageQueryDTO dto, String role, Long relatedId);

    List<StudentScoreVO> queryStudentScore(Long studentId, String role, Long relatedId);

    void confirmScore(Long scoreId, Long userId, String role, Long relatedId);

    byte[] exportScores(String role, Long relatedId);
}

