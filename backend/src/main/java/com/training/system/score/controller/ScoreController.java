package com.training.system.score.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.score.dto.*;
import com.training.system.score.service.ScoreService;
import com.training.system.score.vo.ProgressVO;
import com.training.system.score.vo.ScoreVO;
import com.training.system.score.vo.StageTaskVO;
import com.training.system.score.vo.StudentScoreVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreController {
    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/stage-task")
    public Result<Void> createStageTask(@Valid @RequestBody StageTaskCreateDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        scoreService.createStageTask(dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    @GetMapping("/stage-task/page")
    public Result<PageResult<StageTaskVO>> queryStageTaskPage(@Valid ScorePageQueryDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        return Result.success(scoreService.queryStageTaskPage(dto, user.role, user.relatedId));
    }

    @PostMapping("/stage-evaluation")
    public Result<Void> evaluateStage(@Valid @RequestBody StageEvaluationDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        scoreService.evaluateStage(dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    @GetMapping("/progress")
    public Result<List<ProgressVO>> queryProgress(@RequestParam(value = "teamId", required = false) Long teamId,
                                                  HttpSession session) {
        UserSession user = getUserSession(session);
        return Result.success(scoreService.queryProgress(teamId, user.role, user.relatedId));
    }

    @PostMapping("/contribution")
    public Result<Void> recordContribution(@Valid @RequestBody ContributionDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        scoreService.recordContribution(dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    @PostMapping
    public Result<Void> saveTeamScore(@Valid @RequestBody ScoreSaveDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        scoreService.saveTeamScore(dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<ScoreVO>> queryScorePage(@Valid ScorePageQueryDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        return Result.success(scoreService.queryScorePage(dto, user.role, user.relatedId));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<StudentScoreVO>> queryStudentScore(@PathVariable Long studentId, HttpSession session) {
        UserSession user = getUserSession(session);
        return Result.success(scoreService.queryStudentScore(studentId, user.role, user.relatedId));
    }

    @PostMapping("/{scoreId}/confirm")
    public Result<Void> confirmScore(@PathVariable Long scoreId, HttpSession session) {
        UserSession user = getUserSession(session);
        scoreService.confirmScore(scoreId, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportScores(HttpSession session) {
        UserSession user = getUserSession(session);
        byte[] content = scoreService.exportScores(user.role, user.relatedId);
        String filename = URLEncoder.encode("score-export.csv", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(content);
    }

    private UserSession getUserSession(HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        Object relatedId = session.getAttribute("relatedId");

        if (userId == null) {
            return new UserSession(4L, "TEACHER", 1L);
        }
        return new UserSession((Long) userId, (String) role, (Long) relatedId);
    }

    private record UserSession(Long userId, String role, Long relatedId) {}
}

