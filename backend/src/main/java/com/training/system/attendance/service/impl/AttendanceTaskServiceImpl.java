package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.AttendanceTaskCreateDTO;
import com.training.system.attendance.dto.AttendanceTaskQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.entity.AttendanceRecord;
import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.enums.AttendanceScopeTypeEnum;
import com.training.system.attendance.enums.AttendanceTaskStatusEnum;
import com.training.system.attendance.enums.AttendanceTaskTypeEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.mapper.AttendanceTaskMapper;
import com.training.system.attendance.service.AttendanceTaskService;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.attendance.utils.ScopeValidator;
import com.training.system.info.entity.Student;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.info.mapper.TeacherMapper;
import com.training.system.selection.mapper.TeamMemberMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 签到任务 Service 实现
 */
@Service
public class AttendanceTaskServiceImpl implements AttendanceTaskService {

    private final AttendanceTaskMapper attendanceTaskMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final ScopeValidator scopeValidator;

    public AttendanceTaskServiceImpl(AttendanceTaskMapper attendanceTaskMapper,
                                     AttendanceRecordMapper attendanceRecordMapper,
                                     StudentMapper studentMapper,
                                     TeacherMapper teacherMapper,
                                     TeamMemberMapper teamMemberMapper,
                                     ScopeValidator scopeValidator) {
        this.attendanceTaskMapper = attendanceTaskMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.scopeValidator = scopeValidator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(AttendanceTaskCreateDTO dto, CurrentUserDTO user) {
        if (!user.isTeacher() && !user.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限发布签到任务");
        }

        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }

        // 适用范围校验
        Integer scopeType = dto.getScopeType();
        String scopeValue = dto.getScopeValue();
        if (scopeType == null || (scopeType != 1 && scopeType != 2 && scopeType != 3)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "适用范围类型不正确");
        }
        if ((scopeType == 1 || scopeType == 2) && (scopeValue == null || scopeValue.isBlank())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请至少选择一个班级或团队");
        }

        List<Long> studentIds = resolveScopeStudents(scopeType, scopeValue);

        AttendanceTask task = new AttendanceTask();
        BeanUtils.copyProperties(dto, task);
        task.setTeacherId(user.getRelatedId());
        task.setStatus(AttendanceTaskStatusEnum.NOT_STARTED.getCode());
        attendanceTaskMapper.insert(task);

        for (Long studentId : studentIds) {
            attendanceRecordMapper.insertInitRecord(task.getTaskId(), studentId);
        }

        return task.getTaskId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishTask(Long taskId, CurrentUserDTO user) {
        if (!user.isTeacher() && !user.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限结束签到任务");
        }

        AttendanceTask task = attendanceTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "签到任务不存在");
        }

        if (user.isTeacher() && !task.getTeacherId().equals(user.getRelatedId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能结束本人发布的任务");
        }

        if (task.getStatus() != null && task.getStatus().equals(AttendanceTaskStatusEnum.FINISHED.getCode())) {
            throw new BusinessException(ResultCode.CONFLICT, "该签到任务已结束");
        }

        attendanceTaskMapper.updateStatus(taskId, AttendanceTaskStatusEnum.FINISHED.getCode());
    }

    @Override
    public PageResult<AttendanceTaskVO> page(AttendanceTaskQueryDTO dto, CurrentUserDTO user) {
        Long teacherId = user.isTeacher() ? user.getRelatedId() : null;
        Long studentId = user.isStudent() ? user.getRelatedId() : null;

        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        long total = attendanceTaskMapper.countByCondition(dto.getKeyword(), teacherId, studentId, dto.getStatus());
        List<AttendanceTaskVO> list = attendanceTaskMapper.selectPage(
                dto.getKeyword(), teacherId, studentId, dto.getStatus(), offset, dto.getPageSize());

        // 批量填充教师名称
        Map<Long, String> teacherNameMap = buildTeacherNameMap(list);
        for (AttendanceTaskVO vo : list) {
            fillTaskVoNames(vo, teacherNameMap);
        }

        return new PageResult<>(list, total, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public AttendanceTaskDetailVO detail(Long taskId, CurrentUserDTO user) {
        AttendanceTask task = attendanceTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "签到任务不存在");
        }

        checkTaskVisible(task, user);

        AttendanceTaskDetailVO detail = attendanceTaskMapper.selectDetailById(taskId);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "签到任务不存在");
        }
        Map<Long, String> teacherNameMap = buildTeacherNameMap(List.of(detail));
        fillTaskVoNames(detail, teacherNameMap);

        // 查询该任务下考勤记录（不分页）
        Long detailStudentId = user.isStudent() ? user.getRelatedId() : null;
        List<AttendanceRecordVO> records = attendanceRecordMapper.selectPage(
                taskId, detailStudentId, null, null, null, null, null, 0, Integer.MAX_VALUE);

        // 批量填充学生信息
        List<Long> studentIds = records.stream()
                .map(AttendanceRecordVO::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Student> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectByIds(studentIds);
            for (Student s : students) {
                studentMap.put(s.getStudentId(), s);
            }
        }

        for (AttendanceRecordVO record : records) {
            record.setSignStatusName(com.training.system.attendance.enums.AttendanceSignStatusEnum
                    .getDescByCode(record.getSignStatus()));
            Student student = studentMap.get(record.getStudentId());
            if (student != null) {
                record.setStudentNo(student.getStudentNo());
                record.setStudentName(student.getStudentName());
                record.setClassName(student.getClassName());
            }
        }
        detail.setRecords(records);

        return detail;
    }

    private List<Long> resolveScopeStudents(Integer scopeType, String scopeValue) {
        return switch (scopeType) {
            case 1 -> studentMapper.selectIdsByClassName(scopeValue);
            case 2 -> {
                Long teamId;
                try {
                    teamId = Long.valueOf(scopeValue);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "团队ID格式不正确");
                }
                yield teamMemberMapper.selectStudentIdsByTeamId(teamId);
            }
            case 3 -> studentMapper.selectAllIds();
            default -> new ArrayList<>();
        };
    }

    private void checkTaskVisible(AttendanceTask task, CurrentUserDTO user) {
        if (user.isAdmin()) {
            return;
        }
        if (user.isTeacher()) {
            if (!task.getTeacherId().equals(user.getRelatedId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看该任务");
            }
            return;
        }
        if (user.isStudent() && !scopeValidator.isStudentInScope(task, user.getRelatedId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不在本次签到范围内");
        }
    }

    private void fillTaskVoNames(AttendanceTaskVO vo, Map<Long, String> teacherNameMap) {
        if (vo == null) {
            return;
        }
        vo.setTaskTypeName(AttendanceTaskTypeEnum.getDescByCode(vo.getTaskType()));
        vo.setScopeTypeName(AttendanceScopeTypeEnum.getDescByCode(vo.getScopeType()));
        vo.setScopeDisplayName(buildScopeDisplayName(vo.getScopeType(), vo.getScopeValue()));

        Integer realStatus = computeTaskStatus(vo);
        vo.setStatus(realStatus);
        vo.setStatusName(AttendanceTaskStatusEnum.getDescByCode(realStatus));

        vo.setTeacherName(teacherNameMap.getOrDefault(vo.getTeacherId(), null));
    }

    private Map<Long, String> buildTeacherNameMap(List<? extends AttendanceTaskVO> vos) {
        List<Long> teacherIds = vos.stream()
                .map(AttendanceTaskVO::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> map = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            List<Map<String, Object>> rows = teacherMapper.selectTeacherNamesByIds(teacherIds);
            for (Map<String, Object> row : rows) {
                Long id = (Long) row.get("teacher_id");
                String name = (String) row.get("teacher_name");
                if (id != null) {
                    map.put(id, name);
                }
            }
        }
        return map;
    }

    private String buildScopeDisplayName(Integer scopeType, String scopeValue) {
        if (scopeType == null) {
            return "";
        }
        return switch (scopeType) {
            case 1 -> scopeValue == null ? "" : scopeValue;
            case 2 -> scopeValue == null ? "" : "团队" + scopeValue;
            case 3 -> "全部";
            default -> "";
        };
    }

    private Integer computeTaskStatus(AttendanceTaskVO vo) {
        if (vo == null) {
            return null;
        }
        // 数据库已标记结束的，保持结束
        if (vo.getStatus() != null && vo.getStatus() == AttendanceTaskStatusEnum.FINISHED.getCode()) {
            return vo.getStatus();
        }
        LocalDateTime now = LocalDateTime.now();
        if (vo.getStartTime() != null && now.isBefore(vo.getStartTime())) {
            return AttendanceTaskStatusEnum.NOT_STARTED.getCode();
        }
        if (vo.getEndTime() != null && now.isAfter(vo.getEndTime())) {
            return AttendanceTaskStatusEnum.FINISHED.getCode();
        }
        return AttendanceTaskStatusEnum.IN_PROGRESS.getCode();
    }
}
