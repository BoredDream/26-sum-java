package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.AttendanceStatisticsQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.service.AttendanceStatisticsService;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.attendance.vo.AttendanceStatisticsVO;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 考勤统计 Service 实现
 */
@Service
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceRecordMapper attendanceRecordMapper;

    public AttendanceStatisticsServiceImpl(AttendanceRecordMapper attendanceRecordMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
    }

    @Override
    public AttendanceStatisticsVO statistics(AttendanceStatisticsQueryDTO dto, CurrentUserDTO user) {
        if (!user.isTeacher() && !user.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看考勤统计");
        }

        Long teacherId = user.isTeacher() ? user.getRelatedId() : null;
        List<AttendanceRecordVO> records = attendanceRecordMapper.selectListForStatistics(
                dto.getTaskId(), dto.getStudentId(), dto.getClassName(), dto.getTeamId(),
                dto.getStartDate(), dto.getEndDate(), teacherId);

        int totalCount = records.size();
        int normalCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int makeupCount = 0;

        for (AttendanceRecordVO record : records) {
            Integer status = record.getSignStatus();
            if (status == null) {
                status = AttendanceSignStatusEnum.ABSENT.getCode();
            }
            switch (status) {
                case 1 -> normalCount++;
                case 2 -> lateCount++;
                case 3 -> makeupCount++;
                default -> absentCount++;
            }
        }

        BigDecimal attendanceRate = BigDecimal.ZERO;
        if (totalCount > 0) {
            int effective = normalCount + makeupCount;
            attendanceRate = BigDecimal.valueOf(effective)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        }

        AttendanceStatisticsVO vo = new AttendanceStatisticsVO();
        vo.setTotalCount(totalCount);
        vo.setNormalCount(normalCount);
        vo.setLateCount(lateCount);
        vo.setAbsentCount(absentCount);
        vo.setMakeupCount(makeupCount);
        vo.setAttendanceRate(attendanceRate);
        vo.setLateTimes(lateCount);
        vo.setAbsentTimes(absentCount);
        return vo;
    }
}
