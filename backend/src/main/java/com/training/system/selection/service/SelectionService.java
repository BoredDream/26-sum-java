package com.training.system.selection.service;

import com.training.system.selection.dto.AuditSelectionDTO;
import com.training.system.selection.dto.SubmitSelectionDTO;
import com.training.system.selection.entity.TeamEntity;
import com.training.system.selection.entity.TopicEntity;
import com.training.system.selection.entity.TopicSelectionEntity;
import com.training.system.selection.mapper.TeamMapper;
import com.training.system.selection.mapper.TeamMemberMapper;
import com.training.system.selection.mapper.TopicMapper;
import com.training.system.selection.mapper.TopicSelectionMapper;
import com.training.system.selection.vo.SelectionVO;
import com.training.system.selection.vo.TopicVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.training.system.selection.service.SelectionConstants.*;

@Service
public class SelectionService {
    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TopicMapper topicMapper;
    private final TopicSelectionMapper topicSelectionMapper;

    public SelectionService(TeamService teamService,
                            TeamMapper teamMapper,
                            TeamMemberMapper teamMemberMapper,
                            TopicMapper topicMapper,
                            TopicSelectionMapper topicSelectionMapper) {
        this.teamService = teamService;
        this.teamMapper = teamMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.topicMapper = topicMapper;
        this.topicSelectionMapper = topicSelectionMapper;
    }

    public List<TopicVO> listOpenTopics(String keyword) {
        return topicMapper.findOpenTopics(keyword == null ? null : keyword.trim())
                .stream().map(this::toTopicVO).toList();
    }

    public TopicVO getTopicDetail(Long topicId) {
        return toTopicVO(getTopicById(topicId));
    }

    @Transactional(rollbackFor = Exception.class)
    public SelectionVO submitSelection(Long userId, String role, SubmitSelectionDTO dto) {
        teamService.requireStudent(role);
        TeamEntity team = teamService.getCurrentTeamByStudent(userId);
        teamService.assertTeamLeader(userId, role, team.getId());
        if (TEAM_SELECTED.equals(team.getStatus())) {
            throw new SelectionBusinessException("当前团队已完成选题，不能重复提交");
        }
        if (topicSelectionMapper.findPendingByTeamId(team.getId()) != null) {
            throw new SelectionBusinessException("当前团队已有待审核选题申请");
        }

        TopicEntity topic = getTopicById(dto.getTopicId());
        validateTopicCanBeSelected(topic);
        validateTeamSize(team.getId(), topic);

        TopicSelectionEntity selection = new TopicSelectionEntity();
        selection.setTeamId(team.getId());
        selection.setTopicId(topic.getId());
        selection.setSelectionReason(dto.getSelectionReason().trim());
        selection.setStatus(SELECTION_PENDING);
        selection.setApplyTime(LocalDateTime.now());
        topicSelectionMapper.insert(selection);

        return selectionView(selection, team, topic);
    }

    public List<SelectionVO> getMySelections(Long userId, String role) {
        teamService.requireStudent(role);
        TeamEntity team = teamService.getCurrentTeamByStudent(userId);
        return topicSelectionMapper.findViewByTeamId(team.getId());
    }

    public List<SelectionVO> getPendingSelections(Long userId, String role) {
        teamService.requireTeacherOrAdmin(role);
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return topicSelectionMapper.findAllPending();
        }
        return topicSelectionMapper.findPendingByTeacherId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public SelectionVO auditSelection(Long userId, String role, Long selectionId, AuditSelectionDTO dto) {
        teamService.requireTeacherOrAdmin(role);
        TopicSelectionEntity selection = topicSelectionMapper.findById(selectionId);
        if (selection == null) {
            throw new SelectionBusinessException(404, "选题申请不存在");
        }
        if (!SELECTION_PENDING.equals(selection.getStatus())) {
            throw new SelectionBusinessException("该选题申请已经审核");
        }
        if (!Boolean.TRUE.equals(dto.getApproved()) && isBlank(dto.getOpinion())) {
            throw new SelectionBusinessException("驳回选题时必须填写审核意见");
        }

        TopicEntity topic = getTopicById(selection.getTopicId());
        if (ROLE_TEACHER.equalsIgnoreCase(role) && !userId.equals(topic.getTeacherId())) {
            throw new SelectionBusinessException(403, "只能审核本人发布课题的选题申请");
        }

        TeamEntity team = teamService.getTeamById(selection.getTeamId());
        if (Boolean.TRUE.equals(dto.getApproved())) {
            validateTopicCanBeSelected(topic);
            if (TEAM_SELECTED.equals(team.getStatus())) {
                throw new SelectionBusinessException("该团队已经完成其他课题的选题");
            }
            validateTeamSize(team.getId(), topic);
            selection.setStatus(SELECTION_APPROVED);
            team.setStatus(TEAM_SELECTED);
            team.setSelectedTopicId(topic.getId());
            teamMapper.updateSelectionStatus(team);
            topicMapper.updateStatus(topic.getId(), TOPIC_SELECTED);
        } else {
            selection.setStatus(SELECTION_REJECTED);
        }
        selection.setAuditTeacherId(userId);
        selection.setAuditOpinion(dto.getOpinion());
        selection.setAuditTime(LocalDateTime.now());
        topicSelectionMapper.audit(selection);
        return selectionView(selection, team, topic);
    }

    public TopicEntity getTopicById(Long topicId) {
        TopicEntity topic = topicMapper.findById(topicId);
        if (topic == null) {
            throw new SelectionBusinessException(404, "课题不存在");
        }
        return topic;
    }

    private void validateTopicCanBeSelected(TopicEntity topic) {
        if (!TOPIC_OPEN.equals(topic.getStatus())) {
            throw new SelectionBusinessException("该课题当前不可选择");
        }
        LocalDateTime now = LocalDateTime.now();
        if (topic.getSelectionStart() != null && now.isBefore(topic.getSelectionStart())) {
            throw new SelectionBusinessException("选题尚未开始");
        }
        if (topic.getSelectionEnd() != null && now.isAfter(topic.getSelectionEnd())) {
            throw new SelectionBusinessException("选题时间已结束");
        }
    }

    private void validateTeamSize(Long teamId, TopicEntity topic) {
        int count = teamMemberMapper.countActiveByTeamId(teamId);
        if (count < topic.getMinMembers() || count > topic.getMaxMembers()) {
            throw new SelectionBusinessException("当前团队人数为" + count + "人，不符合课题要求（" +
                    topic.getMinMembers() + "-" + topic.getMaxMembers() + "人）");
        }
    }

    private TopicVO toTopicVO(TopicEntity entity) {
        TopicVO vo = new TopicVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setDirection(entity.getDirection());
        vo.setDifficulty(entity.getDifficulty());
        vo.setTeacherId(entity.getTeacherId());
        vo.setMinMembers(entity.getMinMembers());
        vo.setMaxMembers(entity.getMaxMembers());
        vo.setStatus(entity.getStatus());
        vo.setSelectionStart(entity.getSelectionStart());
        vo.setSelectionEnd(entity.getSelectionEnd());
        return vo;
    }

    private SelectionVO selectionView(TopicSelectionEntity selection, TeamEntity team, TopicEntity topic) {
        SelectionVO vo = new SelectionVO();
        vo.setSelectionId(selection.getId());
        vo.setTeamId(selection.getTeamId());
        vo.setTeamName(team.getTeamName());
        vo.setTopicId(selection.getTopicId());
        vo.setTopicTitle(topic.getTitle());
        vo.setSelectionReason(selection.getSelectionReason());
        vo.setStatus(selection.getStatus());
        vo.setAuditTeacherId(selection.getAuditTeacherId());
        vo.setAuditOpinion(selection.getAuditOpinion());
        vo.setApplyTime(selection.getApplyTime());
        vo.setAuditTime(selection.getAuditTime());
        return vo;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
