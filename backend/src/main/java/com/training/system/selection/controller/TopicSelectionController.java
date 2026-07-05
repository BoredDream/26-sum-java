package com.training.system.selection.controller;

import com.training.system.selection.dto.AuditSelectionDTO;
import com.training.system.selection.dto.SubmitSelectionDTO;
import com.training.system.selection.service.SelectionService;
import com.training.system.selection.vo.ApiResponse;
import com.training.system.selection.vo.SelectionVO;
import com.training.system.selection.vo.TopicVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selection")
public class TopicSelectionController {
    private final SelectionService selectionService;

    public TopicSelectionController(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @GetMapping("/topics")
    public ApiResponse<List<TopicVO>> listTopics(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(selectionService.listOpenTopics(keyword));
    }

    @GetMapping("/topics/{topicId}")
    public ApiResponse<TopicVO> getTopicDetail(@PathVariable Long topicId) {
        return ApiResponse.success(selectionService.getTopicDetail(topicId));
    }

    @PostMapping("/applications")
    public ApiResponse<SelectionVO> submitSelection(@RequestHeader("X-User-Id") Long userId,
                                                     @RequestHeader("X-Role") String role,
                                                     @RequestBody @Valid SubmitSelectionDTO dto) {
        return ApiResponse.success("选题申请已提交，等待教师审核", selectionService.submitSelection(userId, role, dto));
    }

    @GetMapping("/applications/my")
    public ApiResponse<List<SelectionVO>> getMySelections(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader("X-Role") String role) {
        return ApiResponse.success(selectionService.getMySelections(userId, role));
    }

    @GetMapping("/applications/pending")
    public ApiResponse<List<SelectionVO>> getPendingSelections(@RequestHeader("X-User-Id") Long userId,
                                                                @RequestHeader("X-Role") String role) {
        return ApiResponse.success(selectionService.getPendingSelections(userId, role));
    }

    @PatchMapping("/applications/{selectionId}/audit")
    public ApiResponse<SelectionVO> auditSelection(@RequestHeader("X-User-Id") Long userId,
                                                    @RequestHeader("X-Role") String role,
                                                    @PathVariable Long selectionId,
                                                    @RequestBody @Valid AuditSelectionDTO dto) {
        return ApiResponse.success("选题申请已审核", selectionService.auditSelection(userId, role, selectionId, dto));
    }
}
