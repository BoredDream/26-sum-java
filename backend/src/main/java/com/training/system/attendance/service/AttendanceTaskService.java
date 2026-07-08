package com.training.system.attendance.service;

import com.training.system.attendance.dto.AttendanceTaskCreateDTO;
import com.training.system.attendance.dto.AttendanceTaskQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import com.training.system.common.PageResult;

/**
 * 签到任务 Service
 */
public interface AttendanceTaskService {

    Long createTask(AttendanceTaskCreateDTO dto, CurrentUserDTO user);

    void finishTask(Long taskId, CurrentUserDTO user);

    PageResult<AttendanceTaskVO> page(AttendanceTaskQueryDTO dto, CurrentUserDTO user);

    AttendanceTaskDetailVO detail(Long taskId, CurrentUserDTO user);

    void deleteTask(Long taskId, CurrentUserDTO user);
}
