package com.training.system.attendance.controller;

import com.training.system.attendance.dto.AttendanceTaskCreateDTO;
import com.training.system.attendance.dto.AttendanceTaskQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.service.AttendanceTaskService;
import com.training.system.attendance.utils.SessionUtil;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import com.training.system.common.PageResult;
import com.training.system.common.Result;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 签到任务 Controller
 */
@RestController
@RequestMapping("/api/attendance/task")
public class AttendanceTaskController {

    private final AttendanceTaskService attendanceTaskService;

    public AttendanceTaskController(AttendanceTaskService attendanceTaskService) {
        this.attendanceTaskService = attendanceTaskService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody AttendanceTaskCreateDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        Long taskId = attendanceTaskService.createTask(dto, user);
        return Result.success(taskId);
    }

    @PostMapping("/{taskId}/finish")
    public Result<Void> finish(@PathVariable Long taskId, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        attendanceTaskService.finishTask(taskId, user);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<AttendanceTaskVO>> page(AttendanceTaskQueryDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        PageResult<AttendanceTaskVO> pageResult = attendanceTaskService.page(dto, user);
        return Result.success(pageResult);
    }

    @GetMapping("/{taskId}")
    public Result<AttendanceTaskDetailVO> detail(@PathVariable Long taskId, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        AttendanceTaskDetailVO detail = attendanceTaskService.detail(taskId, user);
        return Result.success(detail);
    }
}
