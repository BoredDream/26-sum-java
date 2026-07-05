package com.training.system.selection.controller;

import com.training.system.selection.dto.CreateDevelopmentLogDTO;
import com.training.system.selection.dto.LogFeedbackDTO;
import com.training.system.selection.service.DevelopmentLogService;
import com.training.system.common.Result;
import com.training.system.selection.vo.DevelopmentLogVO;
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
    public Result<DevelopmentLogVO> create(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestHeader("X-Role") String role,
                                                 @RequestBody @Valid CreateDevelopmentLogDTO dto) {
        return Result.success(developmentLogService.create(userId, role, dto));
    }

    @GetMapping
    public Result<List<DevelopmentLogVO>> listMyScope(@RequestHeader("X-User-Id") Long userId,
                                                            @RequestHeader("X-Role") String role) {
        return Result.success(developmentLogService.listMyScope(userId, role));
    }

    @PatchMapping("/{logId}/feedback")
    public Result<DevelopmentLogVO> feedback(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestHeader("X-Role") String role,
                                                   @PathVariable Long logId,
                                                   @RequestBody @Valid LogFeedbackDTO dto) {
        return Result.success(developmentLogService.feedback(userId, role, logId, dto));
    }
}
