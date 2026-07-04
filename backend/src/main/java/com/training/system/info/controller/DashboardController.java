package com.training.system.info.controller;

import com.training.system.common.Result;
import com.training.system.info.service.BackupService;
import com.training.system.info.service.LogService;
import com.training.system.info.service.NoticeService;
import com.training.system.info.service.StudentService;
import com.training.system.info.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private BackupService backupService;
    @Autowired
    private LogService logService;

    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("studentCount", studentService.count());
        data.put("teacherCount", teacherService.count());
        data.put("noticeCount", noticeService.count());
        data.put("backupCount", backupService.count());
        data.put("logCount", logService.count());
        return Result.success(data);
    }
}
