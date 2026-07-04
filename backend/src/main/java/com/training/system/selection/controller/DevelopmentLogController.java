package com.training.system.selection.controller;

import com.training.system.selection.dto.CreateDevelopmentLogDTO;
import com.training.system.selection.dto.LogFeedbackDTO;
import com.training.system.selection.service.DevelopmentLogService;
import com.training.system.selection.vo.ApiResponse;
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
    public ApiResponse<DevelopmentLogVO> create(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestHeader("X-Role") String role,
                                                 @RequestBody @Valid CreateDevelopmentLogDTO dto) {
        return ApiResponse.success("开发日志提交成功", developmentLogService.create(userId, role, dto));
    }

    @GetMapping
    public ApiResponse<List<DevelopmentLogVO>> listMyScope(@RequestHeader("X-User-Id") Long userId,
                                                            @RequestHeader("X-Role") String role) {
        return ApiResponse.success(developmentLogService.listMyScope(userId, role));
    }

    @PatchMapping("/{logId}/feedback")
    public ApiResponse<DevelopmentLogVO> feedback(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestHeader("X-Role") String role,
                                                   @PathVariable Long logId,
                                                   @RequestBody @Valid LogFeedbackDTO dto) {
        return ApiResponse.success("开发日志反馈已保存", developmentLogService.feedback(userId, role, logId, dto));
    }
}
