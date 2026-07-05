package com.training.system.selection.service;

import com.training.system.exception.BusinessException;
import com.training.system.common.ResultCode;

import com.training.system.selection.dto.CreateDevelopmentLogDTO;
import com.training.system.selection.dto.LogFeedbackDTO;
import com.training.system.selection.entity.DevelopmentLogEntity;
import com.training.system.selection.entity.TeamEntity;
import com.training.system.selection.entity.TopicEntity;
import com.training.system.selection.mapper.DevelopmentLogMapper;
import com.training.system.selection.mapper.TopicMapper;
import com.training.system.selection.vo.DevelopmentLogVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.training.system.selection.service.SelectionConstants.*;

@Service
public class DevelopmentLogService {
    private final TeamService teamService;
    private final TopicMapper topicMapper;
    private final DevelopmentLogMapper logMapper;

    public DevelopmentLogService(TeamService teamService,
                                 TopicMapper topicMapper,
                                 DevelopmentLogMapper logMapper) {
        this.teamService = teamService;
        this.topicMapper = topicMapper;
        this.logMapper = logMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public DevelopmentLogVO create(Long userId, String role, CreateDevelopmentLogDTO dto) {
        teamService.requireStudent(role);
        TeamEntity team = teamService.getCurrentTeamByStudent(userId);
        if (!TEAM_SELECTED.equals(team.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请在选题审核通过后再填写开发日志");
        }
        if (logMapper.findByStudentAndDate(userId, dto.getLogDate()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当天已经提交过开发日志，请修改已有记录或选择其他日期");
        }

        DevelopmentLogEntity log = new DevelopmentLogEntity();
        log.setTeamId(team.getId());
        log.setStudentId(userId);
        log.setTitle(dto.getTitle().trim());
        log.setLogDate(dto.getLogDate());
        log.setWorkContent(dto.getWorkContent().trim());
        log.setCompletionStatus(dto.getCompletionStatus().trim());
        log.setProblemDescription(dto.getProblemDescription());
        log.setNextPlan(dto.getNextPlan());
        LocalDateTime now = LocalDateTime.now();
        log.setCreateTime(now);
        log.setUpdateTime(now);
        logMapper.insert(log);
        return toVO(log);
    }

    public List<DevelopmentLogVO> listMyScope(Long userId, String role) {
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            TeamEntity team = teamService.getCurrentTeamByStudent(userId);
            return logMapper.findByTeamId(team.getId()).stream().map(this::toVO).toList();
        }
        if (ROLE_TEACHER.equalsIgnoreCase(role)) {
            return logMapper.findByTeacherId(userId).stream().map(this::toVO).toList();
        }
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return logMapper.findAll().stream().map(this::toVO).toList();
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无查看权限");
    }

    @Transactional(rollbackFor = Exception.class)
    public DevelopmentLogVO feedback(Long userId, String role, Long logId, LogFeedbackDTO dto) {
        teamService.requireTeacherOrAdmin(role);
        DevelopmentLogEntity log = getLogById(logId);
        assertTeacherCanAccess(userId, role, log.getTeamId());
        log.setTeacherFeedback(dto.getFeedback().trim());
        log.setFeedbackTeacherId(userId);
        log.setUpdateTime(LocalDateTime.now());
        logMapper.updateFeedback(log);
        return toVO(log);
    }

    private DevelopmentLogEntity getLogById(Long id) {
        DevelopmentLogEntity log = logMapper.findById(id);
        if (log == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "开发日志不存在");
        }
        return log;
    }

    private void assertTeacherCanAccess(Long userId, String role, Long teamId) {
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return;
        }
        TeamEntity team = teamService.getTeamById(teamId);
        if (team.getSelectedTopicId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该团队尚未完成选题");
        }
        TopicEntity topic = topicMapper.findById(team.getSelectedTopicId());
        if (topic == null || !userId.equals(topic.getTeacherId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权反馈该团队的开发日志");
        }
    }

    private DevelopmentLogVO toVO(DevelopmentLogEntity entity) {
        DevelopmentLogVO vo = new DevelopmentLogVO();
        vo.setId(entity.getId());
        vo.setTeamId(entity.getTeamId());
        vo.setStudentId(entity.getStudentId());
        vo.setTitle(entity.getTitle());
        vo.setLogDate(entity.getLogDate());
        vo.setWorkContent(entity.getWorkContent());
        vo.setCompletionStatus(entity.getCompletionStatus());
        vo.setProblemDescription(entity.getProblemDescription());
        vo.setNextPlan(entity.getNextPlan());
        vo.setTeacherFeedback(entity.getTeacherFeedback());
        vo.setFeedbackTeacherId(entity.getFeedbackTeacherId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
