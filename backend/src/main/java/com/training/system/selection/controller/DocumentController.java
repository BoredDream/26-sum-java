package com.training.system.selection.controller;

import com.training.system.info.annotation.OperationLog;
import com.training.system.selection.dto.DocumentFeedbackDTO;
import com.training.system.selection.service.DocumentService;
import com.training.system.common.Result;
import com.training.system.selection.util.SelectionSessionUtil;
import com.training.system.selection.util.SelectionSessionUtil.CurrentUser;
import com.training.system.selection.vo.ProcessDocumentVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/selection/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @OperationLog(type = "CREATE", description = "上传过程文档")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ProcessDocumentVO> upload(HttpSession session,
                                            @RequestParam String documentName,
                                            @RequestParam String documentType,
                                            @RequestParam String projectStage,
                                            @RequestPart MultipartFile file) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(documentService.upload(user.relatedId(), user.role(), documentName, documentType, projectStage, file));
    }

    @GetMapping
    public Result<List<ProcessDocumentVO>> listMyScope(HttpSession session) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(documentService.listMyScope(user.relatedId(), user.role()));
    }

    @OperationLog(type = "UPDATE", description = "反馈过程文档")
    @PatchMapping("/{documentId}/feedback")
    public Result<ProcessDocumentVO> feedback(HttpSession session,
                                              @PathVariable Long documentId,
                                              @RequestBody @Valid DocumentFeedbackDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(documentService.feedback(user.relatedId(), user.role(), documentId, dto));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(HttpSession session,
                                             @PathVariable Long documentId) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        Resource resource = documentService.download(user.relatedId(), user.role(), documentId);
        String filename = resource.getFilename() == null ? "download" : resource.getFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .body(resource);
    }
}
