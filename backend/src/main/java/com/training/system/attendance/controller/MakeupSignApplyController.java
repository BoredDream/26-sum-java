package com.training.system.attendance.controller;

import com.training.system.info.annotation.OperationLog;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.dto.MakeupApplyDTO;
import com.training.system.attendance.dto.MakeupApplyQueryDTO;
import com.training.system.attendance.dto.MakeupReviewDTO;
import com.training.system.attendance.service.MakeupSignApplyService;
import com.training.system.attendance.utils.SessionUtil;
import com.training.system.attendance.vo.MakeupApplyVO;
import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 补签申请 Controller
 */
@RestController
@RequestMapping("/api/attendance/makeup")
public class MakeupSignApplyController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "doc", "docx");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    private final MakeupSignApplyService makeupSignApplyService;
    private final String uploadPath;

    public MakeupSignApplyController(MakeupSignApplyService makeupSignApplyService,
                                     @Value("${app.upload.path:D:/training-uploads/}") String uploadPath) {
        this.makeupSignApplyService = makeupSignApplyService;
        this.uploadPath = uploadPath;
    }

    @OperationLog(type = "CREATE", description = "提交补签申请")
    @PostMapping
    public Result<Long> apply(@Valid @RequestBody MakeupApplyDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        Long applyId = makeupSignApplyService.applyMakeup(dto, user);
        return Result.success(applyId);
    }

    @OperationLog(type = "UPDATE", description = "审核补签申请")
    @PostMapping("/{applyId}/review")
    public Result<Void> review(@PathVariable Long applyId,
                               @Valid @RequestBody MakeupReviewDTO dto,
                               HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        makeupSignApplyService.reviewMakeup(applyId, dto, user);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<MakeupApplyVO>> page(MakeupApplyQueryDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        PageResult<MakeupApplyVO> pageResult = makeupSignApplyService.page(dto, user);
        return Result.success(pageResult);
    }

    @GetMapping("/unviewed-count")
    public Result<Long> unviewedCount(HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        if (!user.isStudent()) {
            return Result.success(0L);
        }
        return Result.success(makeupSignApplyService.countUnviewedResults(user.getRelatedId()));
    }

    @PostMapping("/mark-viewed")
    public Result<Void> markViewed(HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        if (!user.isStudent()) {
            return Result.success();
        }
        makeupSignApplyService.markResultsViewed(user.getRelatedId());
        return Result.success();
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        if (!user.isStudent()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可上传补签证明材料");
        }
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过50MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的文件格式");
        }

        String newName = UUID.randomUUID() + "." + extension;
        String relativeDir = "attendance";
        Path targetDir = Paths.get(uploadPath, relativeDir);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path targetPath = targetDir.resolve(newName);
        file.transferTo(targetPath.toFile());

        return Result.success(relativeDir + "/" + newName);
    }

    @GetMapping("/{applyId}/download")
    public void download(@PathVariable Long applyId, jakarta.servlet.http.HttpServletResponse response) {
        String proofPath = makeupSignApplyService.getProofPath(applyId);
        if (proofPath == null) {
            try { response.sendError(404, "证明材料不存在"); } catch (IOException ignored) {}
            return;
        }
        Path file = Paths.get(uploadPath, proofPath);
        if (!Files.exists(file)) {
            try { response.sendError(404, "文件未找到"); } catch (IOException ignored) {}
            return;
        }
        try {
            String encoded = java.net.URLEncoder.encode(file.getFileName().toString(),
                    java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            Files.copy(file, response.getOutputStream());
        } catch (IOException e) {
            try { response.sendError(500); } catch (IOException ignored) {}
        }
    }
}
