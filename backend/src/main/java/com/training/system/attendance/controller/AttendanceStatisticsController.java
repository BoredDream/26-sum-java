package com.training.system.attendance.controller;

import com.training.system.attendance.dto.AttendanceStatisticsQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.service.AttendanceStatisticsService;
import com.training.system.attendance.utils.SessionUtil;
import com.training.system.attendance.vo.AttendanceStatisticsVO;
import com.training.system.common.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤统计 Controller
 */
@RestController
@RequestMapping("/api/attendance/statistics")
public class AttendanceStatisticsController {

    private final AttendanceStatisticsService attendanceStatisticsService;

    public AttendanceStatisticsController(AttendanceStatisticsService attendanceStatisticsService) {
        this.attendanceStatisticsService = attendanceStatisticsService;
    }

    @GetMapping
    public Result<AttendanceStatisticsVO> statistics(AttendanceStatisticsQueryDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        AttendanceStatisticsVO vo = attendanceStatisticsService.statistics(dto, user);
        return Result.success(vo);
    }
}
