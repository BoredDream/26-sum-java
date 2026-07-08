package com.training.system.attendance.service;

import com.training.system.attendance.dto.AttendanceRecordQueryDTO;
import com.training.system.attendance.dto.AttendanceSignDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.common.PageResult;

import java.util.List;

/**
 * 考勤记录 Service
 */
public interface AttendanceRecordService {

    AttendanceRecordVO sign(AttendanceSignDTO dto, CurrentUserDTO user);

    PageResult<AttendanceRecordVO> page(AttendanceRecordQueryDTO dto, CurrentUserDTO user);

    List<AttendanceRecordVO> listForExport(AttendanceRecordQueryDTO dto, CurrentUserDTO user);

    /**
     * 统计学生未签到的进行中任务数
     */
    long countUnsignedTasks(Long studentId);

    /**
     * 查询学生已签到的进行中任务ID列表
     */
    java.util.List<Long> getSignedTaskIds(Long studentId);
}
