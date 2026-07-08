package com.training.system.score.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.score.dto.*;
import com.training.system.score.entity.Contribution;
import com.training.system.score.entity.ScoreRecord;
import com.training.system.score.entity.StageEvaluation;
import com.training.system.score.entity.StageTask;
import com.training.system.score.entity.StudentScore;
import com.training.system.score.mapper.ContributionMapper;
import com.training.system.score.mapper.ScoreMapper;
import com.training.system.score.mapper.StageEvaluationMapper;
import com.training.system.score.mapper.StageTaskMapper;
import com.training.system.score.service.ScoreService;
import com.training.system.score.vo.ProgressVO;
import com.training.system.score.vo.ScoreVO;
import com.training.system.score.vo.StageTaskVO;
import com.training.system.score.vo.StudentScoreVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ScoreServiceImpl implements ScoreService {
    private static final Map<Integer, String> STAGE_STATUS_TEXT = Map.of(
            0, "未开始",
            1, "进行中",
            2, "已结束"
    );
    private static final Map<Integer, String> SCORE_STATUS_TEXT = Map.of(
            0, "草稿",
            1, "已确认",
            2, "已锁定"
    );

    private final StageTaskMapper stageTaskMapper;
    private final StageEvaluationMapper stageEvaluationMapper;
    private final ContributionMapper contributionMapper;
    private final ScoreMapper scoreMapper;

    public ScoreServiceImpl(StageTaskMapper stageTaskMapper,
                            StageEvaluationMapper stageEvaluationMapper,
                            ContributionMapper contributionMapper,
                            ScoreMapper scoreMapper) {
        this.stageTaskMapper = stageTaskMapper;
        this.stageEvaluationMapper = stageEvaluationMapper;
        this.contributionMapper = contributionMapper;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public void createStageTask(StageTaskCreateDTO dto, Long userId, String role, Long relatedId) {
        requireTeacherOrAdmin(role, "无阶段任务发布权限");
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "截止时间不能早于开始时间");
        }
        validateRange(dto.getWeight(), BigDecimal.ZERO, new BigDecimal("100"), "权重必须在0-100之间");

        StageTask stageTask = new StageTask();
        stageTask.setStageName(dto.getStageName());
        stageTask.setStageDesc(dto.getStageDesc());
        stageTask.setStartTime(dto.getStartTime());
        stageTask.setEndTime(dto.getEndTime());
        stageTask.setDeliverables(dto.getDeliverables());
        stageTask.setScoringCriteria(dto.getScoringCriteria());
        stageTask.setWeight(dto.getWeight());
        stageTask.setTeacherId(relatedId);
        stageTask.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        stageTaskMapper.insert(stageTask);
    }

    @Override
    public PageResult<StageTaskVO> queryStageTaskPage(ScorePageQueryDTO dto, String role, Long relatedId) {
        normalizePage(dto);
        Long teacherId = "TEACHER".equals(role) ? relatedId : dto.getTeacherId();
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<StageTaskVO> records = stageTaskMapper.selectPage(
                dto.getKeyword(), teacherId, dto.getStatus(), offset, dto.getPageSize());
        for (StageTaskVO vo : records) {
            vo.setStatusText(stageStatusText(vo.getStatus()));
        }
        long total = stageTaskMapper.countPage(dto.getKeyword(), teacherId, dto.getStatus());
        return new PageResult<>(records, total, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    @Transactional
    public void evaluateStage(StageEvaluationDTO dto, Long userId, String role, Long relatedId) {
        requireTeacherOrAdmin(role, "无阶段评价权限");
        StageTask task = stageTaskMapper.selectById(dto.getStageId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "阶段任务不存在");
        }
        validateRange(dto.getDocScore(), BigDecimal.ZERO, new BigDecimal("100"), "文档质量得分必须在0-100之间");
        validateRange(dto.getCompletionScore(), BigDecimal.ZERO, new BigDecimal("100"), "完成度得分必须在0-100之间");
        validateRange(defaultZero(dto.getInnovationScore()), BigDecimal.ZERO, new BigDecimal("100"), "创新性得分必须在0-100之间");
        validateRange(defaultZero(dto.getTechScore()), BigDecimal.ZERO, new BigDecimal("100"), "技术难度得分必须在0-100之间");
        if (isBlank(dto.getComment())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请填写评价意见");
        }

        StageEvaluation evaluation = stageEvaluationMapper.selectByStageAndTeam(dto.getStageId(), dto.getTeamId());
        boolean exists = evaluation != null;
        if (!exists) {
            evaluation = new StageEvaluation();
        }
        evaluation.setStageId(dto.getStageId());
        evaluation.setTeamId(dto.getTeamId());
        evaluation.setRelatedDocumentId(dto.getRelatedDocumentId());
        evaluation.setDocScore(dto.getDocScore());
        evaluation.setCompletionScore(dto.getCompletionScore());
        evaluation.setInnovationScore(defaultZero(dto.getInnovationScore()));
        evaluation.setTechScore(defaultZero(dto.getTechScore()));
        evaluation.setTotalScore(averageScore(dto.getDocScore(), dto.getCompletionScore(),
                defaultZero(dto.getInnovationScore()), defaultZero(dto.getTechScore())));
        evaluation.setComment(dto.getComment());
        evaluation.setResult(dto.getResult());
        evaluation.setTeacherId(relatedId);
        evaluation.setIsLate(dto.getIsLate() == null ? 0 : dto.getIsLate());
        evaluation.setLateDays(dto.getLateDays() == null ? 0 : dto.getLateDays());

        if (exists) {
            stageEvaluationMapper.update(evaluation);
        } else {
            stageEvaluationMapper.insert(evaluation);
        }
    }

    @Override
    public List<ProgressVO> queryProgress(Long teamId, String role, Long relatedId) {
        return scoreMapper.selectProgress(teamId);
    }

    @Override
    public void recordContribution(ContributionDTO dto, Long userId, String role, Long relatedId) {
        validateRange(dto.getContributionScore(), BigDecimal.ZERO, new BigDecimal("100"), "贡献度评分必须在0-100之间");
        validateRange(dto.getWorkloadRatio(), BigDecimal.ZERO, new BigDecimal("100"), "工作量占比必须在0-100之间");
        Contribution contribution = new Contribution();
        contribution.setTeamId(dto.getTeamId());
        contribution.setStageId(dto.getStageId());
        contribution.setStudentId(dto.getStudentId());
        contribution.setEvaluatorId(userId);
        contribution.setEvaluationType(dto.getEvaluationType());
        contribution.setContributionScore(dto.getContributionScore());
        contribution.setWorkloadRatio(dto.getWorkloadRatio());
        contribution.setWorkDescription(dto.getWorkDescription());
        contribution.setComment(dto.getComment());
        contributionMapper.insert(contribution);
    }

    @Override
    @Transactional
    public void saveTeamScore(ScoreSaveDTO dto, Long userId, String role, Long relatedId) {
        requireTeacherOrAdmin(role, "无成绩保存权限");
        validateScoreItems(dto);
        BigDecimal total = dto.getDocScore()
                .add(dto.getAttendanceScore())
                .add(dto.getSystemScore())
                .add(dto.getDefenseScore())
                .setScale(2, RoundingMode.HALF_UP);

        ScoreRecord existing = scoreMapper.selectByTeamId(dto.getTeamId());
        if (existing != null && existing.getStatus() != null && existing.getStatus() == 2) {
            throw new BusinessException(ResultCode.CONFLICT, "成绩已锁定，不能修改");
        }

        ScoreRecord score = existing == null ? new ScoreRecord() : existing;
        score.setTeamId(dto.getTeamId());
        score.setTeacherId(relatedId);
        score.setAiReportId(dto.getAiReportId());
        score.setDocScore(dto.getDocScore());
        score.setAttendanceScore(dto.getAttendanceScore());
        score.setSystemScore(dto.getSystemScore());
        score.setDefenseScore(dto.getDefenseScore());
        score.setTotalScore(total);
        score.setTeacherComment(dto.getTeacherComment());
        score.setStatus(0);

        if (existing == null) {
            scoreMapper.insertScore(score);
        } else {
            scoreMapper.updateScore(score);
        }
        saveStudentScores(dto, score);
    }

    @Override
    public PageResult<ScoreVO> queryScorePage(ScorePageQueryDTO dto, String role, Long relatedId) {
        normalizePage(dto);
        Long teacherId = "TEACHER".equals(role) ? relatedId : dto.getTeacherId();
        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<ScoreVO> records = scoreMapper.selectScorePage(
                dto.getKeyword(), dto.getTeamId(), teacherId, dto.getStatus(), offset, dto.getPageSize());
        fillScoreStatus(records);
        long total = scoreMapper.countScorePage(dto.getKeyword(), dto.getTeamId(), teacherId, dto.getStatus());
        return new PageResult<>(records, total, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public List<StudentScoreVO> queryStudentScore(Long studentId, String role, Long relatedId) {
        if ("STUDENT".equals(role) && !studentId.equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能查询本人个人成绩");
        }
        return scoreMapper.selectStudentScores(studentId);
    }

    @Override
    public void confirmScore(Long scoreId, Long userId, String role, Long relatedId) {
        requireTeacherOrAdmin(role, "无成绩确认权限");
        ScoreRecord score = scoreMapper.selectById(scoreId);
        if (score == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成绩记录不存在");
        }
        if ("TEACHER".equals(role) && !score.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权确认他人成绩");
        }
        if (score.getStatus() != null && score.getStatus() == 2) {
            throw new BusinessException(ResultCode.CONFLICT, "成绩已锁定");
        }
        scoreMapper.updateScoreStatus(scoreId, 1);
    }

    @Override
    public byte[] exportScores(String role, Long relatedId) {
        requireTeacherOrAdmin(role, "无成绩导出权限");
        Long teacherId = "TEACHER".equals(role) ? relatedId : null;
        List<ScoreVO> scores = scoreMapper.selectScoreExport(teacherId);
        fillScoreStatus(scores);
        StringBuilder csv = new StringBuilder();
        csv.append("scoreId,teamName,teacherName,docScore,attendanceScore,systemScore,defenseScore,totalScore,status\n");
        for (ScoreVO score : scores) {
            csv.append(score.getScoreId()).append(',')
                    .append(escapeCsv(score.getTeamName())).append(',')
                    .append(escapeCsv(score.getTeacherName())).append(',')
                    .append(score.getDocScore()).append(',')
                    .append(score.getAttendanceScore()).append(',')
                    .append(score.getSystemScore()).append(',')
                    .append(score.getDefenseScore()).append(',')
                    .append(score.getTotalScore()).append(',')
                    .append(escapeCsv(score.getStatusText())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void saveStudentScores(ScoreSaveDTO dto, ScoreRecord score) {
        scoreMapper.deleteStudentScoresByScoreId(score.getScoreId());
        if (dto.getStudentScores() == null) {
            return;
        }
        for (StudentScoreSaveDTO studentDto : dto.getStudentScores()) {
            BigDecimal factor = studentDto.getContributionFactor() == null ? BigDecimal.ONE : studentDto.getContributionFactor();
            validateRange(factor, BigDecimal.ZERO, new BigDecimal("2"), "贡献度调整系数必须在0-2之间");
            BigDecimal personalScore = score.getTotalScore().multiply(factor).setScale(2, RoundingMode.HALF_UP);

            StudentScore studentScore = new StudentScore();
            studentScore.setScoreId(score.getScoreId());
            studentScore.setTeamId(score.getTeamId());
            studentScore.setStudentId(studentDto.getStudentId());
            studentScore.setContributionFactor(factor);
            studentScore.setPersonalScore(personalScore);
            studentScore.setGrade(calculateGrade(personalScore));
            studentScore.setTeacherComment(studentDto.getTeacherComment());
            scoreMapper.insertStudentScore(studentScore);
        }
    }

    private void validateScoreItems(ScoreSaveDTO dto) {
        validateRange(dto.getDocScore(), BigDecimal.ZERO, new BigDecimal("15"), "文档成绩必须在0-15之间");
        validateRange(dto.getAttendanceScore(), BigDecimal.ZERO, new BigDecimal("15"), "考勤成绩必须在0-15之间");
        validateRange(dto.getSystemScore(), BigDecimal.ZERO, new BigDecimal("50"), "系统实现与测试成绩必须在0-50之间");
        validateRange(dto.getDefenseScore(), BigDecimal.ZERO, new BigDecimal("20"), "答辩成绩必须在0-20之间");
    }

    private void requireTeacherOrAdmin(String role, String message) {
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, message);
        }
    }

    private void normalizePage(ScorePageQueryDTO dto) {
        if (dto.getPageNum() == null || dto.getPageNum() < 1) {
            dto.setPageNum(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max, String message) {
        if (value == null || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, message);
        }
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal averageScore(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        return a.add(b).add(c).add(d).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
    }

    private String calculateGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) return "优秀";
        if (score.compareTo(new BigDecimal("80")) >= 0) return "良好";
        if (score.compareTo(new BigDecimal("70")) >= 0) return "中等";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "及格";
        return "不及格";
    }

    private void fillScoreStatus(List<ScoreVO> scores) {
        for (ScoreVO score : scores) {
            score.setStatusText(scoreStatusText(score.getStatus()));
        }
    }

    private String stageStatusText(Integer status) {
        return status == null ? "未知" : STAGE_STATUS_TEXT.getOrDefault(status, "未知");
    }

    private String scoreStatusText(Integer status) {
        return status == null ? "未知" : SCORE_STATUS_TEXT.getOrDefault(status, "未知");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
