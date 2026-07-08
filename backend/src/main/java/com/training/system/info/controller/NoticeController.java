package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.annotation.OperationLog;
import com.training.system.info.dto.NoticeCreateDTO;
import com.training.system.info.dto.NoticeQueryDTO;
import com.training.system.info.dto.NoticeUpdateDTO;
import com.training.system.info.service.NoticeService;
import com.training.system.info.vo.NoticeVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @OperationLog(type = "CREATE", description = "发布通知")
    @PostMapping
    public Result<NoticeVO> create(@Valid @ModelAttribute NoticeCreateDTO dto,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   HttpSession session) {
        Long publisherId = (Long) session.getAttribute("userId");
        return Result.success(noticeService.createNotice(dto, publisherId, file));
    }

    @OperationLog(type = "UPDATE", description = "修改通知")
    @PutMapping("/{noticeId}")
    public Result<NoticeVO> update(@PathVariable Long noticeId,
                                   @Valid @ModelAttribute NoticeUpdateDTO dto,
                                   @RequestParam(value = "file", required = false) MultipartFile file) {
        return Result.success(noticeService.updateNotice(noticeId, dto, file));
    }

    @OperationLog(type = "DELETE", description = "删除通知")
    @DeleteMapping("/{noticeId}")
    public Result<Void> delete(@PathVariable Long noticeId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        boolean isAdmin = "ADMIN".equals(role);
        noticeService.deleteNotice(noticeId, userId, isAdmin);
        return Result.success();
    }

    @GetMapping("/read-ids")
    public Result<java.util.List<Long>> readIds(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return Result.success(noticeService.getReadNoticeIds(userId));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            return Result.success(0L);
        }
        return Result.success(noticeService.getUnreadCount(userId));
    }

    @GetMapping("/{noticeId}")
    public Result<NoticeVO> detail(@PathVariable Long noticeId) {
        return Result.success(noticeService.getNoticeDetail(noticeId));
    }

    @GetMapping("/page")
    public Result<PageResult<NoticeVO>> page(NoticeQueryDTO query) {
        return Result.success(noticeService.pageNotices(query));
    }

    @OperationLog(type = "UPDATE", description = "置顶/取消置顶公告")
    @PostMapping("/{noticeId}/top")
    public Result<Void> toggleTop(@PathVariable Long noticeId) {
        noticeService.toggleTop(noticeId);
        return Result.success();
    }

    @PostMapping("/{noticeId}/read")
    public Result<Void> markRead(@PathVariable Long noticeId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            return Result.success();
        }
        noticeService.markAsRead(noticeId, userId);
        return Result.success();
    }

    @GetMapping("/{noticeId}/download")
    public void download(@PathVariable Long noticeId, HttpServletResponse response) {
        try {
            String path = noticeService.getAttachmentPath(noticeId);
            if (path == null) {
                response.sendError(404);
                return;
            }
            File file = new File("./uploads/" + path);
            if (!file.exists()) {
                response.sendError(404);
                return;
            }
            String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            try { response.sendError(500); } catch (Exception ignored) {}
        }
    }
}
