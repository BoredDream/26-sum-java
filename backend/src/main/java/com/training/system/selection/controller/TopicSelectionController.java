package com.training.system.selection.controller;

import com.training.system.selection.dto.AuditSelectionDTO;
import com.training.system.selection.dto.SubmitSelectionDTO;
import com.training.system.selection.service.SelectionService;
import com.training.system.common.Result;
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
    public Result<List<TopicVO>> listTopics(@RequestParam(required = false) String keyword) {
        return Result.success(selectionService.listOpenTopics(keyword));
    }

    @GetMapping("/topics/{topicId}")
    public Result<TopicVO> getTopicDetail(@PathVariable Long topicId) {
        return Result.success(selectionService.getTopicDetail(topicId));
    }

    @PostMapping("/applications")
    public Result<SelectionVO> submitSelection(@RequestHeader("X-User-Id") Long userId,
                                                     @RequestHeader("X-Role") String role,
                                                     @RequestBody @Valid SubmitSelectionDTO dto) {
        return Result.success(selectionService.submitSelection(userId, role, dto));
    }

    @GetMapping("/applications/my")
    public Result<List<SelectionVO>> getMySelections(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader("X-Role") String role) {
        return Result.success(selectionService.getMySelections(userId, role));
    }

    @GetMapping("/applications/pending")
    public Result<List<SelectionVO>> getPendingSelections(@RequestHeader("X-User-Id") Long userId,
                                                                @RequestHeader("X-Role") String role) {
        return Result.success(selectionService.getPendingSelections(userId, role));
    }

    @PatchMapping("/applications/{selectionId}/audit")
    public Result<SelectionVO> auditSelection(@RequestHeader("X-User-Id") Long userId,
                                                    @RequestHeader("X-Role") String role,
                                                    @PathVariable Long selectionId,
                                                    @RequestBody @Valid AuditSelectionDTO dto) {
        return Result.success(selectionService.auditSelection(userId, role, selectionId, dto));
    }
}
