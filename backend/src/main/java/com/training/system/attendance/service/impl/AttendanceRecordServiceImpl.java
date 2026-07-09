package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.AttendanceRecordQueryDTO;
import com.training.system.attendance.dto.AttendanceSignDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.entity.AttendanceRecord;
import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.enums.AttendanceTaskStatusEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.mapper.AttendanceTaskMapper;
import com.training.system.attendance.service.AttendanceRecordService;
import com.training.system.attendance.utils.ScopeValidator;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.common.DistanceUtils;
import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.info.entity.Student;
import com.training.system.info.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤记录 Service 实现
 */
@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceTaskMapper attendanceTaskMapper;
    private final StudentMapper studentMapper;
    private final ScopeValidator scopeValidator;

    public AttendanceRecordServiceImpl(AttendanceRecordMapper attendanceRecordMapper,
                                       AttendanceTaskMapper attendanceTaskMapper,
                                       StudentMapper studentMapper,
                                       ScopeValidator scopeValidator) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceTaskMapper = attendanceTaskMapper;
        this.studentMapper = studentMapper;
        this.scopeValidator = scopeValidator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecordVO sign(AttendanceSignDTO dto, CurrentUserDTO user) {
        if (!user.isStudent()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可签到");
        }

        Long studentId = user.getRelatedId();
        AttendanceTask task = attendanceTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "签到任务不存在");
        }

        if (!scopeValidator.isStudentInScope(task, studentId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不在本次签到范围内");
        }
        if (Integer.valueOf(AttendanceTaskStatusEnum.FINISHED.getCode()).equals(task.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前签到任务已结束");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(task.getStartTime()) || now.isAfter(task.getEndTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前不在签到时间内，请在规定时间内签到");
        }

        // 定位签到校验
        if (task.getRequireLocation() != null && task.getRequireLocation() == 1) {
            if (dto.getSignLng() == null || dto.getSignLat() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "本次签到需要定位，请开启位置权限后重试");
            }
            if (task.getLocationLng() == null || task.getLocationLat() == null) {
                throw new BusinessException(ResultCode.ERROR, "签到任务定位信息不完整，请联系教师");
            }
            double distance = DistanceUtils.haversine(
                    task.getLocationLat().doubleValue(), task.getLocationLng().doubleValue(),
                    dto.getSignLat(), dto.getSignLng()
            );
            int radius = task.getLocationRadius() != null ? task.getLocationRadius() : 500;
            if (distance > radius) {
                double distanceToEdge = distance - radius;
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        String.format("您距离签到范围边缘还有约 %.0f 米（距中心 %.0f 米，允许半径 %d 米），请在指定地点签到",
                                distanceToEdge, distance, radius));
            }
        }

        AttendanceRecord record = attendanceRecordMapper.selectByTaskAndStudent(dto.getTaskId(), studentId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考勤记录不存在");
        }
        if (record.getSignStatus() != null && record.getSignStatus() != AttendanceSignStatusEnum.ABSENT.getCode()) {
            throw new BusinessException(ResultCode.CONFLICT, "您已完成本次签到，请勿重复签到");
        }

        int signStatus = now.isAfter(task.getStartTime().plusMinutes(15))
                ? AttendanceSignStatusEnum.LATE.getCode()
                : AttendanceSignStatusEnum.NORMAL.getCode();

        record.setSignTime(now);
        record.setSignStatus(signStatus);
        record.setRemark(dto.getRemark());
        attendanceRecordMapper.update(record);

        return convertToVO(record, task);
    }

    @Override
    public PageResult<AttendanceRecordVO> page(AttendanceRecordQueryDTO dto, CurrentUserDTO user) {
        Long teacherId = user.isTeacher() ? user.getRelatedId() : null;
        Long studentId = user.isStudent() ? user.getRelatedId() : null;

        // 学生角色强制只查本人；外部传入 studentId 对学生无效
        Long queryStudentId = user.isStudent() ? studentId : dto.getStudentId();

        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        long total = attendanceRecordMapper.countByCondition(
                dto.getTaskId(), queryStudentId, dto.getSignStatus(), dto.getKeyword(),
                dto.getStartDate(), dto.getEndDate(), teacherId);
        List<AttendanceRecordVO> list = attendanceRecordMapper.selectPage(
                dto.getTaskId(), queryStudentId, dto.getSignStatus(), dto.getKeyword(),
                dto.getStartDate(), dto.getEndDate(), teacherId, offset, dto.getPageSize());

        fillRecordVoNames(list);
        return new PageResult<>(list, total, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public java.util.List<Long> getSignedTaskIds(Long studentId) {
        return attendanceRecordMapper.selectSignedTaskIds(studentId);
    }

    @Override
    public long countUnsignedTasks(Long studentId) {
        return attendanceRecordMapper.countUnsignedTasks(studentId);
    }

    @Override
    public List<AttendanceRecordVO> listForExport(AttendanceRecordQueryDTO dto, CurrentUserDTO user) {
        if (!user.isTeacher() && !user.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限导出考勤报表");
        }
        Long teacherId = user.isTeacher() ? user.getRelatedId() : null;

        List<AttendanceRecordVO> list = attendanceRecordMapper.selectListForExport(
                dto.getTaskId(), dto.getStudentId(), dto.getSignStatus(), dto.getKeyword(),
                dto.getStartDate(), dto.getEndDate(), teacherId);
        fillRecordVoNames(list);
        return list;
    }

    private AttendanceRecordVO convertToVO(AttendanceRecord record, AttendanceTask task) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setRecordId(record.getRecordId());
        vo.setTaskId(record.getTaskId());
        vo.setSignTime(record.getSignTime());
        vo.setSignStatus(record.getSignStatus());
        vo.setSignStatusName(AttendanceSignStatusEnum.getDescByCode(record.getSignStatus()));
        vo.setIsMakeup(record.getIsMakeup());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());

        if (task != null) {
            vo.setTaskTitle(task.getTaskTitle());
            vo.setTaskStartTime(task.getStartTime());
            vo.setTaskEndTime(task.getEndTime());
        }
        Student student = studentMapper.selectById(record.getStudentId());
        if (student != null) {
            vo.setStudentId(student.getStudentId());
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
            vo.setClassName(student.getClassName());
        }
        return vo;
    }

    private void fillRecordVoNames(List<AttendanceRecordVO> list) {
        for (AttendanceRecordVO vo : list) {
            vo.setSignStatusName(AttendanceSignStatusEnum.getDescByCode(vo.getSignStatus()));
        }
    }
}
