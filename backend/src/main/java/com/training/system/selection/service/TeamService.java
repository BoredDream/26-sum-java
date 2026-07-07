package com.training.system.selection.service;

import com.training.system.exception.BusinessException;
import com.training.system.common.ResultCode;

import com.training.system.selection.dto.*;
import com.training.system.selection.entity.*;
import com.training.system.selection.mapper.*;
import com.training.system.selection.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.training.system.selection.service.SelectionConstants.*;

@Service
public class TeamService {
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamJoinRequestMapper joinRequestMapper;
    private final TeamLeaveRequestMapper leaveRequestMapper;

    public TeamService(TeamMapper teamMapper,
                       TeamMemberMapper teamMemberMapper,
                       TeamJoinRequestMapper joinRequestMapper,
                       TeamLeaveRequestMapper leaveRequestMapper) {
        this.teamMapper = teamMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.joinRequestMapper = joinRequestMapper;
        this.leaveRequestMapper = leaveRequestMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public TeamVO createTeam(Long userId, String role, CreateTeamDTO dto) {
        requireStudent(role);
        String teamName = dto.getTeamName().trim();
        if (teamMapper.findByName(teamName) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "团队名称已存在");
        }

        TeamEntity team = new TeamEntity();
        team.setTeamName(teamName);
        team.setLeaderId(userId);
        team.setIntroduction(dto.getIntroduction());
        team.setStatus(TEAM_BUILDING);
        team.setMaxSize(dto.getMaxSize() == null ? 6 : dto.getMaxSize());
        LocalDateTime now = LocalDateTime.now();
        team.setCreateTime(now);
        team.setUpdateTime(now);
        teamMapper.insert(team);

        TeamMemberEntity leader = new TeamMemberEntity();
        leader.setTeamId(team.getId());
        leader.setStudentId(userId);
        leader.setMemberRole("LEADER");
        leader.setWorkContent("团队负责人：统筹项目进度与任务分工");
        leader.setEnabled(true);
        teamMemberMapper.insert(leader);
        return getTeamDetail(team.getId());
    }

    // ── 我的团队（多团队） ──

    public List<TeamVO> getMyTeams(Long userId, String role) {
        requireStudent(role);
        List<TeamMemberEntity> memberships = teamMemberMapper.findActiveByStudentId(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }
        return memberships.stream()
                .map(m -> getTeamDetail(m.getTeamId()))
                .toList();
    }

    // ── 团队详情 ──

    public TeamVO getTeamDetail(Long teamId) {
        TeamEntity team = getTeamById(teamId);
        List<TeamMemberVO> members = teamMemberMapper.findActiveByTeamId(teamId).stream().map(this::toMemberVO).toList();
        TeamVO vo = toTeamVO(team);
        vo.setMembers(members);
        vo.setMemberCount(members.size());
        return vo;
    }

    public List<TeamVO> listJoinableTeams() {
        return teamMapper.findJoinableTeams().stream().map(team -> {
            TeamVO vo = toTeamVO(team);
            vo.setMembers(List.of());
            if (team.getMemberCount() != null) {
                vo.setMemberCount(team.getMemberCount());
            }
            return vo;
        }).toList();
    }

    // ── 加入团队申请 ──

    @Transactional(rollbackFor = Exception.class)
    public JoinRequestVO applyJoin(Long userId, String role, Long teamId, JoinTeamDTO dto) {
        requireStudent(role);
        // 检查是否已是该团队成员
        TeamMemberEntity existingMembership = teamMemberMapper.findByTeamIdAndStudentId(teamId, userId);
        if (existingMembership != null && Boolean.TRUE.equals(existingMembership.getEnabled())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已经是该团队的成员");
        }
        TeamEntity team = getTeamById(teamId);
        if (TEAM_SELECTED.equals(team.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该团队已完成选题，不能再加入成员");
        }
        if (teamMemberMapper.countActiveByTeamId(teamId) >= team.getMaxSize()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该团队人数已达到上限");
        }
        if (joinRequestMapper.findPending(teamId, userId) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已提交过入队申请，请等待负责人审核");
        }
        TeamJoinRequestEntity request = new TeamJoinRequestEntity();
        request.setTeamId(teamId);
        request.setApplicantId(userId);
        request.setApplyMessage(dto.getApplyMessage());
        request.setStatus(JOIN_PENDING);
        request.setApplyTime(LocalDateTime.now());
        joinRequestMapper.insert(request);
        return toJoinRequestVO(request);
    }

    public List<JoinRequestVO> listPendingJoinRequests(Long userId, String role, Long teamId) {
        assertTeamLeader(userId, role, teamId);
        return joinRequestMapper.findPendingByTeamId(teamId).stream().map(this::toJoinRequestVO).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public JoinRequestVO auditJoinRequest(Long userId, String role, Long requestId, AuditJoinRequestDTO dto) {
        TeamJoinRequestEntity request = joinRequestMapper.findById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "入队申请不存在");
        }
        assertTeamLeader(userId, role, request.getTeamId());
        if (!JOIN_PENDING.equals(request.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该入队申请已经处理");
        }
        if (!Boolean.TRUE.equals(dto.getApproved()) && isBlank(dto.getOpinion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "驳回申请时必须填写审核意见");
        }

        if (Boolean.TRUE.equals(dto.getApproved())) {
            TeamEntity team = getTeamById(request.getTeamId());
            if (TEAM_SELECTED.equals(team.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "团队已完成选题，不能再添加成员");
            }
            if (teamMemberMapper.countActiveByTeamId(team.getId()) >= team.getMaxSize()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "团队人数已达到上限");
            }
            // 如果该学生之前退出了该团队，reactivate 而非 insert（避免 uk_team_student 冲突）
            TeamMemberEntity existing = teamMemberMapper.findByTeamIdAndStudentId(request.getTeamId(), request.getApplicantId());
            if (existing != null) {
                teamMemberMapper.reactivateMember(request.getTeamId(), request.getApplicantId());
            } else {
                TeamMemberEntity member = new TeamMemberEntity();
                member.setTeamId(team.getId());
                member.setStudentId(request.getApplicantId());
                member.setMemberRole("MEMBER");
                member.setWorkContent("待团队负责人分配");
                member.setEnabled(true);
                teamMemberMapper.insert(member);
            }
            request.setStatus(JOIN_APPROVED);
        } else {
            request.setStatus(JOIN_REJECTED);
        }
        request.setReviewerId(userId);
        request.setReviewOpinion(dto.getOpinion());
        request.setReviewTime(LocalDateTime.now());
        joinRequestMapper.audit(request);
        return toJoinRequestVO(request);
    }

    // ── 离队申请 ──

    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO requestLeave(Long userId, String role, Long teamId, RequestLeaveTeamDTO dto) {
        requireStudent(role);
        TeamEntity team = getTeamById(teamId);
        if (team.getLeaderId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "团队负责人不能申请离队。如需离队，请先转让负责人或将团队解散");
        }
        TeamMemberEntity membership = teamMemberMapper.findByTeamIdAndStudentId(teamId, userId);
        if (membership == null || !Boolean.TRUE.equals(membership.getEnabled())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您不是该团队的成员");
        }
        if (leaveRequestMapper.findPending(teamId, userId) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已有待审核的离队申请，请等待负责人审核");
        }
        TeamLeaveRequestEntity request = new TeamLeaveRequestEntity();
        request.setTeamId(teamId);
        request.setApplicantId(userId);
        request.setLeaveMessage(dto.getLeaveMessage());
        request.setStatus(LEAVE_PENDING);
        request.setApplyTime(LocalDateTime.now());
        leaveRequestMapper.insert(request);
        return toLeaveRequestVO(request);
    }

    public List<LeaveRequestVO> listPendingLeaveRequests(Long userId, String role, Long teamId) {
        assertTeamLeader(userId, role, teamId);
        return leaveRequestMapper.findPendingByTeamId(teamId).stream().map(this::toLeaveRequestVO).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO auditLeaveRequest(Long userId, String role, Long requestId, AuditLeaveRequestDTO dto) {
        TeamLeaveRequestEntity request = leaveRequestMapper.findById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "离队申请不存在");
        }
        assertTeamLeader(userId, role, request.getTeamId());
        if (!LEAVE_PENDING.equals(request.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该离队申请已经处理");
        }
        if (!Boolean.TRUE.equals(dto.getApproved()) && isBlank(dto.getOpinion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "驳回申请时必须填写审核意见");
        }

        if (Boolean.TRUE.equals(dto.getApproved())) {
            teamMemberMapper.deactivateMember(request.getTeamId(), request.getApplicantId());
            request.setStatus(LEAVE_APPROVED);
        } else {
            request.setStatus(LEAVE_REJECTED);
        }
        request.setReviewerId(userId);
        request.setReviewOpinion(dto.getOpinion());
        request.setReviewTime(LocalDateTime.now());
        leaveRequestMapper.audit(request);
        return toLeaveRequestVO(request);
    }

    // ── 成员分工编辑 ──

    public void updateMemberWork(Long userId, String role, Long teamId, Long studentId, UpdateMemberWorkDTO dto) {
        assertTeamLeader(userId, role, teamId);
        TeamMemberEntity member = teamMemberMapper.findByTeamIdAndStudentId(teamId, studentId);
        if (member == null || !Boolean.TRUE.equals(member.getEnabled())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "团队成员不存在或已退出");
        }
        teamMemberMapper.updateWorkContent(teamId, studentId, dto.getWorkContent().trim());
    }

    // ── 团队查询辅助方法 ──

    @Deprecated
    public TeamEntity getCurrentTeamByStudent(Long studentId) {
        List<TeamMemberEntity> memberships = teamMemberMapper.findActiveByStudentId(studentId);
        if (memberships.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建或加入团队");
        }
        return getTeamById(memberships.get(0).getTeamId());
    }

    public TeamEntity getTeamForStudent(Long studentId, Long teamId) {
        TeamMemberEntity membership = teamMemberMapper.findByTeamIdAndStudentId(teamId, studentId);
        if (membership == null || !Boolean.TRUE.equals(membership.getEnabled())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该团队的成员");
        }
        return getTeamById(teamId);
    }

    public List<TeamEntity> getTeamsForStudent(Long studentId) {
        List<TeamMemberEntity> memberships = teamMemberMapper.findActiveByStudentId(studentId);
        if (memberships.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先创建或加入团队");
        }
        return memberships.stream()
                .map(m -> getTeamById(m.getTeamId()))
                .toList();
    }

    public TeamEntity getTeamById(Long teamId) {
        TeamEntity team = teamMapper.findById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "团队不存在");
        }
        return team;
    }

    public boolean isTeamMember(Long teamId, Long studentId) {
        TeamMemberEntity member = teamMemberMapper.findByTeamIdAndStudentId(teamId, studentId);
        return member != null && Boolean.TRUE.equals(member.getEnabled());
    }

    // ── 权限检查 ──

    public void assertTeamLeader(Long userId, String role, Long teamId) {
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return;
        }
        requireStudent(role);
        TeamEntity team = getTeamById(teamId);
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅团队负责人可以执行该操作");
        }
    }

    public void requireStudent(String role) {
        if (!ROLE_STUDENT.equalsIgnoreCase(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该操作仅限学生角色使用");
        }
    }

    public void requireTeacherOrAdmin(String role) {
        if (!ROLE_TEACHER.equalsIgnoreCase(role) && !ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该操作仅限教师或管理员使用");
        }
    }

    // ── VO 映射 ──

    private TeamVO toTeamVO(TeamEntity entity) {
        TeamVO vo = new TeamVO();
        vo.setId(entity.getId());
        vo.setTeamName(entity.getTeamName());
        vo.setLeaderId(entity.getLeaderId());
        vo.setIntroduction(entity.getIntroduction());
        vo.setStatus(entity.getStatus());
        vo.setSelectedTopicId(entity.getSelectedTopicId());
        vo.setMaxSize(entity.getMaxSize());
        vo.setMemberCount(entity.getMemberCount());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private TeamMemberVO toMemberVO(TeamMemberEntity entity) {
        TeamMemberVO vo = new TeamMemberVO();
        vo.setStudentId(entity.getStudentId());
        vo.setMemberRole(entity.getMemberRole());
        vo.setWorkContent(entity.getWorkContent());
        vo.setJoinTime(entity.getJoinTime());
        return vo;
    }

    private JoinRequestVO toJoinRequestVO(TeamJoinRequestEntity entity) {
        JoinRequestVO vo = new JoinRequestVO();
        vo.setId(entity.getId());
        vo.setTeamId(entity.getTeamId());
        vo.setApplicantId(entity.getApplicantId());
        vo.setApplyMessage(entity.getApplyMessage());
        vo.setStatus(entity.getStatus());
        vo.setReviewerId(entity.getReviewerId());
        vo.setReviewOpinion(entity.getReviewOpinion());
        vo.setApplyTime(entity.getApplyTime());
        vo.setReviewTime(entity.getReviewTime());
        return vo;
    }

    private LeaveRequestVO toLeaveRequestVO(TeamLeaveRequestEntity entity) {
        LeaveRequestVO vo = new LeaveRequestVO();
        vo.setId(entity.getId());
        vo.setTeamId(entity.getTeamId());
        vo.setApplicantId(entity.getApplicantId());
        vo.setLeaveMessage(entity.getLeaveMessage());
        vo.setStatus(entity.getStatus());
        vo.setReviewerId(entity.getReviewerId());
        vo.setReviewOpinion(entity.getReviewOpinion());
        vo.setApplyTime(entity.getApplyTime());
        vo.setReviewTime(entity.getReviewTime());
        return vo;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
