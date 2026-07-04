package com.training.system.attendance.controller;

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

    @PostMapping
    public Result<Long> apply(@Valid @RequestBody MakeupApplyDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        Long applyId = makeupSignApplyService.applyMakeup(dto, user);
        return Result.success(applyId);
    }

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

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
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
}
