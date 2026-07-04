package com.training.system.topic.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.topic.dto.TopicCreateDTO;
import com.training.system.topic.dto.TopicQueryDTO;
import com.training.system.topic.dto.TopicReviewDTO;
import com.training.system.topic.dto.TopicUpdateDTO;
import com.training.system.topic.service.TopicService;
import com.training.system.topic.vo.TopicDetailVO;
import com.training.system.topic.vo.TopicFileVO;
import com.training.system.topic.vo.TopicListVO;
import com.training.system.topic.vo.TopicReviewVO;
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

/**
 * 出题管理控制器
 * 接口路径前缀：/api/topic
 */
@RestController
@RequestMapping("/api/topic")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    // ==================== 题目 CRUD ====================

    /**
     * 新增题目（保存草稿或提交审核）
     */
    @PostMapping
    public Result<Void> createTopic(@Valid @RequestBody TopicCreateDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.createTopic(dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    /**
     * 修改题目
     */
    @PutMapping("/{topicId}")
    public Result<Void> updateTopic(@PathVariable Long topicId,
                                     @Valid @RequestBody TopicUpdateDTO dto,
                                     HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.updateTopic(topicId, dto, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    /**
     * 删除题目（逻辑删除，仅管理员）
     */
    @DeleteMapping("/{topicId}")
    public Result<Void> deleteTopic(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.deleteTopic(topicId, user.userId, user.role);
        return Result.success();
    }

    /**
     * 查看题目详情
     */
    @GetMapping("/{topicId}")
    public Result<TopicDetailVO> getTopicDetail(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        TopicDetailVO detail = topicService.getTopicDetail(topicId, user.userId, user.role, user.relatedId);
        return Result.success(detail);
    }

    /**
     * 分页查询题目列表
     */
    @GetMapping("/page")
    public Result<PageResult<TopicListVO>> queryTopicPage(@Valid TopicQueryDTO dto, HttpSession session) {
        UserSession user = getUserSession(session);
        PageResult<TopicListVO> page = topicService.queryTopicPage(dto, user.userId, user.role, user.relatedId);
        return Result.success(page);
    }

    // ==================== 题目流程操作 ====================

    /**
     * 提交题目审核
     */
    @PostMapping("/{topicId}/submit")
    public Result<Void> submitForReview(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.submitForReview(topicId, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    /**
     * 审核题目（仅管理员）
     */
    @PostMapping("/{topicId}/review")
    public Result<Void> reviewTopic(@PathVariable Long topicId,
                                     @Valid @RequestBody TopicReviewDTO dto,
                                     HttpSession session) {
        UserSession user = getUserSession(session);
        if (!"ADMIN".equals(user.role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无审核权限");
        }
        topicService.reviewTopic(topicId, dto, user.userId);
        return Result.success();
    }

    /**
     * 开放题目（仅管理员）
     */
    @PostMapping("/{topicId}/open")
    public Result<Void> openTopic(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.openTopic(topicId, user.userId, user.role);
        return Result.success();
    }

    /**
     * 关闭题目（仅管理员）
     */
    @PostMapping("/{topicId}/close")
    public Result<Void> closeTopic(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.closeTopic(topicId, user.userId, user.role);
        return Result.success();
    }

    // ==================== 题目资料文件 ====================

    /**
     * 上传题目资料
     */
    @PostMapping("/{topicId}/file")
    public Result<Void> uploadFile(@PathVariable Long topicId,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "fileDesc", required = false) String fileDesc,
                                    HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.uploadFile(topicId, file, fileDesc, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    /**
     * 删除资料
     */
    @DeleteMapping("/file/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId, HttpSession session) {
        UserSession user = getUserSession(session);
        topicService.deleteFile(fileId, user.userId, user.role, user.relatedId);
        return Result.success();
    }

    /**
     * 查看题目资料列表
     */
    @GetMapping("/{topicId}/file")
    public Result<List<TopicFileVO>> listFiles(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        List<TopicFileVO> files = topicService.listFiles(topicId, user.userId, user.role, user.relatedId);
        return Result.success(files);
    }

    /**
     * 下载资料文件
     */
    @GetMapping("/file/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, HttpSession session) {
        UserSession user = getUserSession(session);
        Resource resource = topicService.downloadFile(fileId, user.userId, user.role, user.relatedId);
        String originalFilename = topicService.getFileOriginalName(
                fileId, user.userId, user.role, user.relatedId);

        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    // ==================== 审核记录 ====================

    /**
     * 查看题目审核记录
     */
    @GetMapping("/{topicId}/review")
    public Result<List<TopicReviewVO>> listReviews(@PathVariable Long topicId, HttpSession session) {
        UserSession user = getUserSession(session);
        List<TopicReviewVO> reviews = topicService.listReviews(topicId, user.userId, user.role, user.relatedId);
        return Result.success(reviews);
    }

    // ==================== 辅助方法 ====================

    /**
     * 从 Session 获取当前登录用户信息
     * TODO: 登录功能完成后统一改为从 Interceptor 注入
     */
    private UserSession getUserSession(HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        Object relatedId = session.getAttribute("relatedId");

        // 开发阶段：如果 Session 中没有用户信息，返回默认测试用户（教师）
        if (userId == null) {
            return new UserSession(1L, "TEACHER", 1L);
        }

        return new UserSession(
                (Long) userId,
                (String) role,
                (Long) relatedId
        );
    }

    /**
     * 内部类：封装当前用户 Session 信息
     */
    private record UserSession(Long userId, String role, Long relatedId) {}
}
