package com.training.system.score.service.impl;

import com.training.system.common.PageResult;
import com.training.system.exception.BusinessException;
import com.training.system.score.dto.*;
import com.training.system.score.entity.Contribution;
import com.training.system.score.entity.ScoreRecord;
import com.training.system.score.entity.StageEvaluation;
import com.training.system.score.entity.StageTask;
import com.training.system.score.mapper.ContributionMapper;
import com.training.system.score.mapper.ScoreMapper;
import com.training.system.score.mapper.StageEvaluationMapper;
import com.training.system.score.mapper.StageTaskMapper;
import com.training.system.score.vo.ScoreVO;
import com.training.system.score.vo.StageTaskVO;
import com.training.system.score.vo.StudentScoreVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest {
    @Mock
    private StageTaskMapper stageTaskMapper;
    @Mock
    private StageEvaluationMapper stageEvaluationMapper;
    @Mock
    private ContributionMapper contributionMapper;
    @Mock
    private ScoreMapper scoreMapper;

    @Captor
    private ArgumentCaptor<StageTask> stageTaskCaptor;
    @Captor
    private ArgumentCaptor<StageEvaluation> stageEvaluationCaptor;
    @Captor
    private ArgumentCaptor<ScoreRecord> scoreCaptor;
    @Captor
    private ArgumentCaptor<Contribution> contributionCaptor;

    private ScoreServiceImpl service;

    private static final Long TEACHER_USER_ID = 4L;
    private static final Long TEACHER_ID = 1L;
    private static final Long OTHER_TEACHER_ID = 2L;
    private static final Long STUDENT_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new ScoreServiceImpl(stageTaskMapper, stageEvaluationMapper, contributionMapper, scoreMapper);
    }

    @Test
    @DisplayName("发布阶段任务 - 教师正常发布")
    void createStageTask_success() {
        StageTaskCreateDTO dto = buildStageTaskDto();

        service.createStageTask(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID);

        verify(stageTaskMapper).insert(stageTaskCaptor.capture());
        StageTask saved = stageTaskCaptor.getValue();
        assertEquals("需求分析阶段", saved.getStageName());
        assertEquals(TEACHER_ID, saved.getTeacherId());
        assertEquals(0, saved.getStatus());
    }

    @Test
    @DisplayName("发布阶段任务 - 截止时间早于开始时间时失败")
    void createStageTask_invalidTime_throwsException() {
        StageTaskCreateDTO dto = buildStageTaskDto();
        dto.setEndTime(dto.getStartTime().minusDays(1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createStageTask(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID));

        assertTrue(ex.getMessage().contains("截止时间"));
        verify(stageTaskMapper, never()).insert(any());
    }

    @Test
    @DisplayName("查询阶段任务 - 教师只能查询本人任务")
    void queryStageTaskPage_teacherFiltersOwnTasks() {
        when(stageTaskMapper.selectPage(any(), eq(TEACHER_ID), any(), eq(0), eq(10)))
                .thenReturn(List.of(new StageTaskVO()));
        when(stageTaskMapper.countPage(any(), eq(TEACHER_ID), any())).thenReturn(1L);

        ScorePageQueryDTO dto = new ScorePageQueryDTO();
        dto.setTeacherId(OTHER_TEACHER_ID);
        PageResult<StageTaskVO> page = service.queryStageTaskPage(dto, "TEACHER", TEACHER_ID);

        assertEquals(1L, page.getTotal());
        verify(stageTaskMapper, never()).selectPage(any(), eq(OTHER_TEACHER_ID), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("阶段成果评价 - 首次评价时新增记录并计算平均分")
    void evaluateStage_insert_success() {
        when(stageTaskMapper.selectById(1L)).thenReturn(new StageTask());
        when(stageEvaluationMapper.selectByStageAndTeam(1L, 1L)).thenReturn(null);
        StageEvaluationDTO dto = buildEvaluationDto();

        service.evaluateStage(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID);

        verify(stageEvaluationMapper).insert(stageEvaluationCaptor.capture());
        StageEvaluation saved = stageEvaluationCaptor.getValue();
        assertEquals(new BigDecimal("85.00"), saved.getTotalScore());
        assertEquals(TEACHER_ID, saved.getTeacherId());
    }

    @Test
    @DisplayName("阶段成果评价 - 已有评价时更新记录")
    void evaluateStage_update_success() {
        StageEvaluation existing = new StageEvaluation();
        existing.setEvaluationId(9L);
        when(stageTaskMapper.selectById(1L)).thenReturn(new StageTask());
        when(stageEvaluationMapper.selectByStageAndTeam(1L, 1L)).thenReturn(existing);
        StageEvaluationDTO dto = buildEvaluationDto();

        service.evaluateStage(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID);

        verify(stageEvaluationMapper).update(stageEvaluationCaptor.capture());
        assertEquals(9L, stageEvaluationCaptor.getValue().getEvaluationId());
    }

    @Test
    @DisplayName("保存团队成绩 - 计算总分并生成个人成绩")
    void saveTeamScore_insert_success() {
        when(scoreMapper.selectByTeamId(1L)).thenReturn(null);
        doAnswer(invocation -> {
            ScoreRecord score = invocation.getArgument(0);
            score.setScoreId(10L);
            return 1;
        }).when(scoreMapper).insertScore(any(ScoreRecord.class));

        ScoreSaveDTO dto = buildScoreSaveDto();
        service.saveTeamScore(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID);

        verify(scoreMapper).insertScore(scoreCaptor.capture());
        ScoreRecord score = scoreCaptor.getValue();
        assertEquals(new BigDecimal("90.00"), score.getTotalScore());
        verify(scoreMapper, times(2)).insertStudentScore(any());
    }

    @Test
    @DisplayName("保存团队成绩 - 锁定成绩不能修改")
    void saveTeamScore_locked_throwsException() {
        ScoreRecord existing = new ScoreRecord();
        existing.setScoreId(10L);
        existing.setStatus(2);
        when(scoreMapper.selectByTeamId(1L)).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.saveTeamScore(buildScoreSaveDto(), TEACHER_USER_ID, "TEACHER", TEACHER_ID));

        assertTrue(ex.getMessage().contains("锁定"));
        verify(scoreMapper, never()).updateScore(any());
    }

    @Test
    @DisplayName("记录成员贡献度 - 正常保存贡献度记录")
    void recordContribution_success() {
        ContributionDTO dto = buildContributionDto();

        service.recordContribution(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID);

        verify(contributionMapper).insert(contributionCaptor.capture());
        Contribution saved = contributionCaptor.getValue();
        assertEquals(1L, saved.getTeamId());
        assertEquals(2L, saved.getStudentId());
        assertEquals(TEACHER_USER_ID, saved.getEvaluatorId());
        assertEquals(new BigDecimal("88"), saved.getContributionScore());
        assertEquals(new BigDecimal("50"), saved.getWorkloadRatio());
    }

    @Test
    @DisplayName("记录成员贡献度 - 贡献度评分超过100时失败")
    void recordContribution_invalidScore_throwsException() {
        ContributionDTO dto = buildContributionDto();
        dto.setContributionScore(new BigDecimal("101"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.recordContribution(dto, TEACHER_USER_ID, "TEACHER", TEACHER_ID));

        assertTrue(ex.getMessage().contains("贡献度评分"));
        verify(contributionMapper, never()).insert(any());
    }

    @Test
    @DisplayName("确认成绩 - 教师不能确认他人成绩")
    void confirmScore_otherTeacher_throwsException() {
        ScoreRecord score = new ScoreRecord();
        score.setScoreId(10L);
        score.setTeacherId(OTHER_TEACHER_ID);
        score.setStatus(0);
        when(scoreMapper.selectById(10L)).thenReturn(score);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmScore(10L, TEACHER_USER_ID, "TEACHER", TEACHER_ID));

        assertTrue(ex.getMessage().contains("无权"));
        verify(scoreMapper, never()).updateScoreStatus(anyLong(), anyInt());
    }

    @Test
    @DisplayName("查询个人成绩 - 学生只能查本人")
    void queryStudentScore_studentOnlySelf() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryStudentScore(2L, "STUDENT", STUDENT_ID));

        assertTrue(ex.getMessage().contains("本人"));
    }

    @Test
    @DisplayName("导出成绩 - 生成CSV内容")
    void exportScores_success() {
        ScoreVO score = new ScoreVO();
        score.setScoreId(1L);
        score.setTeamName("第一小组");
        score.setTeacherName("赵老师");
        score.setDocScore(new BigDecimal("14"));
        score.setAttendanceScore(new BigDecimal("15"));
        score.setSystemScore(new BigDecimal("45"));
        score.setDefenseScore(new BigDecimal("16"));
        score.setTotalScore(new BigDecimal("90"));
        score.setStatus(1);
        when(scoreMapper.selectScoreExport(TEACHER_ID)).thenReturn(List.of(score));

        String csv = new String(service.exportScores("TEACHER", TEACHER_ID));

        assertTrue(csv.contains("scoreId,teamName"));
        assertTrue(csv.contains("第一小组"));
        assertTrue(csv.contains("已确认"));
    }

    private StageTaskCreateDTO buildStageTaskDto() {
        StageTaskCreateDTO dto = new StageTaskCreateDTO();
        dto.setStageName("需求分析阶段");
        dto.setStageDesc("完成需求分析说明书");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusDays(7));
        dto.setDeliverables("需求分析文档");
        dto.setScoringCriteria("完整性、规范性、可追踪性");
        dto.setWeight(new BigDecimal("20"));
        return dto;
    }

    private StageEvaluationDTO buildEvaluationDto() {
        StageEvaluationDTO dto = new StageEvaluationDTO();
        dto.setStageId(1L);
        dto.setTeamId(1L);
        dto.setDocScore(new BigDecimal("80"));
        dto.setCompletionScore(new BigDecimal("90"));
        dto.setInnovationScore(new BigDecimal("85"));
        dto.setTechScore(new BigDecimal("85"));
        dto.setComment("阶段成果完整");
        dto.setResult(1);
        return dto;
    }

    private ScoreSaveDTO buildScoreSaveDto() {
        ScoreSaveDTO dto = new ScoreSaveDTO();
        dto.setTeamId(1L);
        dto.setDocScore(new BigDecimal("14"));
        dto.setAttendanceScore(new BigDecimal("15"));
        dto.setSystemScore(new BigDecimal("45"));
        dto.setDefenseScore(new BigDecimal("16"));
        dto.setTeacherComment("整体完成较好");

        StudentScoreSaveDTO s1 = new StudentScoreSaveDTO();
        s1.setStudentId(1L);
        s1.setContributionFactor(new BigDecimal("1.00"));
        StudentScoreSaveDTO s2 = new StudentScoreSaveDTO();
        s2.setStudentId(2L);
        s2.setContributionFactor(new BigDecimal("0.90"));
        dto.setStudentScores(List.of(s1, s2));
        return dto;
    }

    private ContributionDTO buildContributionDto() {
        ContributionDTO dto = new ContributionDTO();
        dto.setTeamId(1L);
        dto.setStageId(1L);
        dto.setStudentId(2L);
        dto.setEvaluationType(2);
        dto.setContributionScore(new BigDecimal("88"));
        dto.setWorkloadRatio(new BigDecimal("50"));
        dto.setWorkDescription("完成后端接口与测试");
        dto.setComment("贡献稳定");
        return dto;
    }
}
