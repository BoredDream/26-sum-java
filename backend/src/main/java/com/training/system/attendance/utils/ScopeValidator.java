package com.training.system.attendance.utils;

import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.info.entity.Student;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.selection.mapper.TeamMemberMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 签到范围校验工具
 * <p>
 * 将 isStudentInScope 抽取为共享组件，避免在多个 Service 中重复实现。
 * </p>
 */
@Component
public class ScopeValidator {

    private final StudentMapper studentMapper;
    private final TeamMemberMapper teamMemberMapper;

    public ScopeValidator(StudentMapper studentMapper, TeamMemberMapper teamMemberMapper) {
        this.studentMapper = studentMapper;
        this.teamMemberMapper = teamMemberMapper;
    }

    /**
     * 判断指定学生是否在签到任务的适用范围内。
     *
     * @param task      签到任务
     * @param studentId 学生 ID
     * @return true 表示在范围内
     */
    public boolean isStudentInScope(AttendanceTask task, Long studentId) {
        if (task == null || studentId == null) {
            return false;
        }
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return false;
        }
        Integer scopeType = task.getScopeType();
        if (scopeType == null) {
            return false;
        }
        return switch (scopeType) {
            case 1 -> task.getScopeValue() != null
                    && task.getScopeValue().equals(student.getClassName());
            case 2 -> {
                Long teamId;
                try {
                    teamId = Long.valueOf(task.getScopeValue());
                } catch (NumberFormatException e) {
                    yield false;
                }
                List<Long> teamStudentIds = teamMemberMapper.selectStudentIdsByTeamId(teamId);
                yield teamStudentIds.contains(studentId);
            }
            case 3 -> true;
            default -> false;
        };
    }
}
