package com.training.system.selection.controller;

import com.training.system.info.annotation.OperationLog;
import com.training.system.selection.dto.AuditSelectionDTO;
import com.training.system.selection.dto.SubmitSelectionDTO;
import com.training.system.selection.service.SelectionService;
import com.training.system.common.Result;
import com.training.system.selection.util.SelectionSessionUtil;
import com.training.system.selection.util.SelectionSessionUtil.CurrentUser;
import com.training.system.selection.vo.SelectionVO;
import com.training.system.selection.vo.TopicVO;
import jakarta.servlet.http.HttpSession;
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

    @OperationLog(type = "CREATE", description = "提交选题申请")
    @PostMapping("/applications")
    public Result<SelectionVO> submitSelection(HttpSession session,
                                               @RequestBody @Valid SubmitSelectionDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(selectionService.submitSelection(user.relatedId(), user.role(), dto));
    }

    @GetMapping("/applications/my")
    public Result<List<SelectionVO>> getMySelections(HttpSession session,
                                                      @RequestParam(required = false) Long teamId) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(selectionService.getMySelections(user.relatedId(), user.role(), teamId));
    }

    @GetMapping("/applications/pending")
    public Result<List<SelectionVO>> getPendingSelections(HttpSession session) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(selectionService.getPendingSelections(user.relatedId(), user.role()));
    }

    @OperationLog(type = "UPDATE", description = "审核选题申请")
    @PatchMapping("/applications/{selectionId}/audit")
    public Result<SelectionVO> auditSelection(HttpSession session,
                                              @PathVariable Long selectionId,
                                              @RequestBody @Valid AuditSelectionDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(selectionService.auditSelection(user.relatedId(), user.role(), selectionId, dto));
    }
}
