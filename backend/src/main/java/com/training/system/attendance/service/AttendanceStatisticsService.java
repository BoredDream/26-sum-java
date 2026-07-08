package com.training.system.attendance.service;

import com.training.system.attendance.dto.AttendanceStatisticsQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.vo.AttendanceStatisticsVO;

/**
 * 考勤统计 Service
 */
public interface AttendanceStatisticsService {

    AttendanceStatisticsVO statistics(AttendanceStatisticsQueryDTO dto, CurrentUserDTO user);
}
