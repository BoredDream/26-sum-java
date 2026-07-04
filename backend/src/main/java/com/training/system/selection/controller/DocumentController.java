package com.training.system.selection.controller;

import com.training.system.selection.dto.DocumentFeedbackDTO;
import com.training.system.selection.service.DocumentService;
import com.training.system.selection.vo.ApiResponse;
import com.training.system.selection.vo.ProcessDocumentVO;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProcessDocumentVO> upload(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestHeader("X-Role") String role,
                                                 @RequestParam String documentName,
                                                 @RequestParam String documentType,
                                                 @RequestParam String projectStage,
                                                 @RequestPart MultipartFile file) {
        return ApiResponse.success("过程文档上传成功",
                documentService.upload(userId, role, documentName, documentType, projectStage, file));
    }

    @GetMapping
    public ApiResponse<List<ProcessDocumentVO>> listMyScope(@RequestHeader("X-User-Id") Long userId,
                                                            @RequestHeader("X-Role") String role) {
        return ApiResponse.success(documentService.listMyScope(userId, role));
    }

    @PatchMapping("/{documentId}/feedback")
    public ApiResponse<ProcessDocumentVO> feedback(@RequestHeader("X-User-Id") Long userId,
                                                    @RequestHeader("X-Role") String role,
                                                    @PathVariable Long documentId,
                                                    @RequestBody @Valid DocumentFeedbackDTO dto) {
        return ApiResponse.success("文档反馈已保存", documentService.feedback(userId, role, documentId, dto));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(@RequestHeader("X-User-Id") Long userId,
                                             @RequestHeader("X-Role") String role,
                                             @PathVariable Long documentId) {
        Resource resource = documentService.download(userId, role, documentId);
        String filename = resource.getFilename() == null ? "download" : resource.getFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .body(resource);
    }
}
