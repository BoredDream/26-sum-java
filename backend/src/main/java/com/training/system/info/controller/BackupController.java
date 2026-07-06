package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.annotation.OperationLog;
import com.training.system.info.service.BackupService;
import com.training.system.info.vo.BackupVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @OperationLog(type = "BACKUP", description = "手动备份数据")
    @PostMapping
    public Result<BackupVO> manualBackup(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return Result.success(backupService.manualBackup(userId));
    }

    @GetMapping("/page")
    public Result<PageResult<BackupVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "15") int pageSize) {
        return Result.success(backupService.pageBackups(pageNum, pageSize));
    }

    @GetMapping("/{backupId}/download")
    public void download(@PathVariable Long backupId, HttpServletResponse response) {
        try {
            String filePath = backupService.getFilePath(backupId);
            File file = new File(filePath);
            if (!file.exists()) { response.sendError(404); return; }
            String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            try { response.sendError(500); } catch (Exception ignored) {}
        }
    }

    @OperationLog(type = "RESTORE", description = "恢复备份数据")
    @PostMapping("/{backupId}/restore")
    public Result<Void> restore(@PathVariable Long backupId) {
        backupService.restoreBackup(backupId);
        return Result.success();
    }

    @OperationLog(type = "DELETE", description = "删除备份文件")
    @DeleteMapping("/{backupId}")
    public Result<Void> delete(@PathVariable Long backupId) {
        backupService.deleteBackup(backupId);
        return Result.success();
    }

    @PostMapping("/cleanup")
    public Result<Void> cleanup() {
        backupService.cleanupOldBackups();
        return Result.success();
    }
}
