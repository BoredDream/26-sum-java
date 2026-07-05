package com.training.system.selection.controller;

import com.training.system.selection.dto.CreateDevelopmentLogDTO;
import com.training.system.selection.dto.LogFeedbackDTO;
import com.training.system.selection.service.DevelopmentLogService;
import com.training.system.common.Result;
import com.training.system.selection.util.SelectionSessionUtil;
import com.training.system.selection.util.SelectionSessionUtil.CurrentUser;
import com.training.system.selection.vo.DevelopmentLogVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selection/logs")
public class DevelopmentLogController {
    private final DevelopmentLogService developmentLogService;

    public DevelopmentLogController(DevelopmentLogService developmentLogService) {
        this.developmentLogService = developmentLogService;
    }

    @PostMapping
    public Result<DevelopmentLogVO> create(HttpSession session,
                                           @RequestBody @Valid CreateDevelopmentLogDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(developmentLogService.create(user.relatedId(), user.role(), dto));
    }

    @GetMapping
    public Result<List<DevelopmentLogVO>> listMyScope(HttpSession session) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(developmentLogService.listMyScope(user.relatedId(), user.role()));
    }

    @PatchMapping("/{logId}/feedback")
    public Result<DevelopmentLogVO> feedback(HttpSession session,
                                             @PathVariable Long logId,
                                             @RequestBody @Valid LogFeedbackDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(developmentLogService.feedback(user.relatedId(), user.role(), logId, dto));
    }
}
