package com.training.system.selection.service;

import com.training.system.exception.BusinessException;
import com.training.system.selection.dto.AuditJoinRequestDTO;
import com.training.system.selection.dto.CreateTeamDTO;
import com.training.system.selection.dto.JoinTeamDTO;
import com.training.system.selection.dto.UpdateMemberWorkDTO;
import com.training.system.selection.entity.TeamEntity;
import com.training.system.selection.entity.TeamJoinRequestEntity;
import com.training.system.selection.entity.TeamMemberEntity;
import com.training.system.selection.mapper.TeamJoinRequestMapper;
import com.training.system.selection.mapper.TeamMapper;
import com.training.system.selection.mapper.TeamMemberMapper;
import com.training.system.selection.vo.JoinRequestVO;
import com.training.system.selection.vo.TeamVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.training.system.selection.service.SelectionConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private TeamJoinRequestMapper joinRequestMapper;

    @Captor
    private ArgumentCaptor<TeamEntity> teamCaptor;
    @Captor
    private ArgumentCaptor<TeamMemberEntity> memberCaptor;
    @Captor
    private ArgumentCaptor<TeamJoinRequestEntity> requestCaptor;

    private TeamService service;

    @BeforeEach
    void setUp() {
        service = new TeamService(teamMapper, teamMemberMapper, joinRequestMapper);
    }

    @Test
    @DisplayName("创建团队 - 学生创建成功并自动成为负责人")
    void createTeam_success() {
        CreateTeamDTO dto = new CreateTeamDTO();
        dto.setTeamName("  第一小组  ");
        dto.setIntroduction("综合实训项目组");
        dto.setMaxSize(5);

        doAnswer(invocation -> {
            TeamEntity team = invocation.getArgument(0);
            team.setId(10L);
            return 1;
        }).when(teamMapper).insert(any(TeamEntity.class));
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 5));
        when(teamMemberMapper.findActiveByTeamId(10L)).thenReturn(List.of(member(10L, 1L, "LEADER", true)));

        TeamVO result = service.createTeam(1L, ROLE_STUDENT, dto);

        assertEquals(10L, result.getId());
        assertEquals(1, result.getMemberCount());
        verify(teamMapper).insert(teamCaptor.capture());
        assertEquals("第一小组", teamCaptor.getValue().getTeamName());
        assertEquals(TEAM_BUILDING, teamCaptor.getValue().getStatus());
        verify(teamMemberMapper).insert(memberCaptor.capture());
        assertEquals("LEADER", memberCaptor.getValue().getMemberRole());
        assertEquals(1L, memberCaptor.getValue().getStudentId());
    }

    @Test
    @DisplayName("创建团队 - 非学生角色禁止创建")
    void createTeam_nonStudentForbidden() {
        CreateTeamDTO dto = new CreateTeamDTO();
        dto.setTeamName("第一小组");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createTeam(1L, ROLE_TEACHER, dto));

        assertTrue(ex.getMessage().contains("仅限学生"));
        verify(teamMapper, never()).insert(any());
    }

    @Test
    @DisplayName("查询可加入团队 - 返回团队列表并包含人数")
    void listJoinableTeams_success() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null, 5);
        team.setMemberCount(2);
        when(teamMapper.findJoinableTeams()).thenReturn(List.of(team));

        List<TeamVO> result = service.listJoinableTeams();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(2, result.get(0).getMemberCount());
        assertNotNull(result.get(0).getMembers());
        assertTrue(result.get(0).getMembers().isEmpty());
    }

    @Test
    @DisplayName("申请入队 - 已加入团队时禁止重复申请")
    void applyJoin_alreadyInTeamThrows() {
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(member(10L, 2L, "MEMBER", true));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.applyJoin(2L, ROLE_STUDENT, 10L, new JoinTeamDTO()));

        assertTrue(ex.getMessage().contains("不能重复申请"));
        verify(joinRequestMapper, never()).insert(any());
    }

    @Test
    @DisplayName("申请入队 - 团队满员时拒绝申请")
    void applyJoin_fullTeamThrows() {
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(null);
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 2));
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(2);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.applyJoin(2L, ROLE_STUDENT, 10L, new JoinTeamDTO()));

        assertTrue(ex.getMessage().contains("达到上限"));
        verify(joinRequestMapper, never()).insert(any());
    }

    @Test
    @DisplayName("审核入队 - 负责人通过后新增团队成员并更新申请")
    void auditJoinRequest_approveSuccess() {
        TeamJoinRequestEntity request = request(99L, 10L, 2L, JOIN_PENDING);
        when(joinRequestMapper.findById(99L)).thenReturn(request);
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(1);
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(null);

        AuditJoinRequestDTO dto = new AuditJoinRequestDTO();
        dto.setApproved(true);
        dto.setOpinion("同意");

        JoinRequestVO result = service.auditJoinRequest(1L, ROLE_STUDENT, 99L, dto);

        assertEquals(JOIN_APPROVED, result.getStatus());
        verify(teamMemberMapper).insert(memberCaptor.capture());
        assertEquals(2L, memberCaptor.getValue().getStudentId());
        assertEquals("MEMBER", memberCaptor.getValue().getMemberRole());
        verify(joinRequestMapper).audit(requestCaptor.capture());
        assertEquals(JOIN_APPROVED, requestCaptor.getValue().getStatus());
        assertEquals(1L, requestCaptor.getValue().getReviewerId());
    }

    @Test
    @DisplayName("审核入队 - 驳回时必须填写审核意见")
    void auditJoinRequest_rejectWithoutOpinionThrows() {
        when(joinRequestMapper.findById(99L)).thenReturn(request(99L, 10L, 2L, JOIN_PENDING));
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));

        AuditJoinRequestDTO dto = new AuditJoinRequestDTO();
        dto.setApproved(false);
        dto.setOpinion(" ");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.auditJoinRequest(1L, ROLE_STUDENT, 99L, dto));

        assertTrue(ex.getMessage().contains("审核意见"));
        verify(teamMemberMapper, never()).insert(any());
        verify(joinRequestMapper, never()).audit(any());
    }

    @Test
    @DisplayName("更新成员分工 - 非负责人禁止更新")
    void updateMemberWork_nonLeaderThrows() {
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));
        UpdateMemberWorkDTO dto = new UpdateMemberWorkDTO();
        dto.setWorkContent("负责测试");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateMemberWork(2L, ROLE_STUDENT, 10L, 3L, dto));

        assertTrue(ex.getMessage().contains("负责人"));
        verify(teamMemberMapper, never()).updateWorkContent(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("我的团队 - 学生查看当前团队详情")
    void getMyTeam_success() {
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(member(10L, 2L, "MEMBER", true));
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 5));
        when(teamMemberMapper.findActiveByTeamId(10L)).thenReturn(List.of(
                member(10L, 1L, "LEADER", true),
                member(10L, 2L, "MEMBER", true)
        ));

        TeamVO result = service.getMyTeam(2L, ROLE_STUDENT);

        assertEquals(10L, result.getId());
        assertEquals(2, result.getMemberCount());
        assertEquals(2, result.getMembers().size());
    }

    @Test
    @DisplayName("我的团队 - 非学生禁止查看")
    void getMyTeam_nonStudentThrows() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getMyTeam(1L, ROLE_TEACHER));

        assertTrue(ex.getMessage().contains("仅限学生"));
    }

    @Test
    @DisplayName("团队详情 - 返回团队与成员信息")
    void getTeamDetail_success() {
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 5));
        when(teamMemberMapper.findActiveByTeamId(10L)).thenReturn(List.of(
                member(10L, 1L, "LEADER", true),
                member(10L, 2L, "MEMBER", true)
        ));

        TeamVO result = service.getTeamDetail(10L);

        assertEquals(10L, result.getId());
        assertEquals(2, result.getMemberCount());
        assertEquals("LEADER", result.getMembers().get(0).getMemberRole());
    }

    @Test
    @DisplayName("待审核入队申请 - 负责人查看列表")
    void listPendingJoinRequests_success() {
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 5));
        when(joinRequestMapper.findPendingByTeamId(10L)).thenReturn(List.of(
                request(99L, 10L, 2L, JOIN_PENDING),
                request(100L, 10L, 3L, JOIN_PENDING)
        ));

        List<JoinRequestVO> result = service.listPendingJoinRequests(1L, ROLE_STUDENT, 10L);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("申请入队 - 学生成功提交申请")
    void applyJoin_success() {
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(null);
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 5));
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(2);
        when(joinRequestMapper.findPending(10L, 2L)).thenReturn(null);

        JoinTeamDTO dto = new JoinTeamDTO();
        dto.setApplyMessage("希望加入团队");

        JoinRequestVO result = service.applyJoin(2L, ROLE_STUDENT, 10L, dto);

        assertEquals(JOIN_PENDING, result.getStatus());
        verify(joinRequestMapper).insert(requestCaptor.capture());
        assertEquals(10L, requestCaptor.getValue().getTeamId());
        assertEquals(2L, requestCaptor.getValue().getApplicantId());
    }

    @Test
    @DisplayName("申请入队 - 人数达到上限前最后一个名额允许申请")
    void applyJoin_oneSlotRemaining_success() {
        when(teamMemberMapper.findActiveByStudentId(2L)).thenReturn(null);
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(2);
        when(joinRequestMapper.findPending(10L, 2L)).thenReturn(null);

        JoinRequestVO result = service.applyJoin(2L, ROLE_STUDENT, 10L, new JoinTeamDTO());

        assertEquals(JOIN_PENDING, result.getStatus());
    }

    @Test
    @DisplayName("审核入队 - 负责人驳回申请")
    void auditJoinRequest_rejectSuccess() {
        TeamJoinRequestEntity request = request(99L, 10L, 2L, JOIN_PENDING);
        when(joinRequestMapper.findById(99L)).thenReturn(request);
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));

        AuditJoinRequestDTO dto = new AuditJoinRequestDTO();
        dto.setApproved(false);
        dto.setOpinion("团队人员已满");

        JoinRequestVO result = service.auditJoinRequest(1L, ROLE_STUDENT, 99L, dto);

        assertEquals(JOIN_REJECTED, result.getStatus());
        assertEquals("团队人员已满", result.getReviewOpinion());
        verify(teamMemberMapper, never()).insert(any());
        verify(joinRequestMapper).audit(requestCaptor.capture());
        assertEquals(1L, requestCaptor.getValue().getReviewerId());
    }

    @Test
    @DisplayName("更新成员分工 - 负责人成功更新并 trim 内容")
    void updateMemberWork_success() {
        when(teamMapper.findById(10L)).thenReturn(team(10L, 1L, TEAM_BUILDING, null, 3));
        TeamMemberEntity member = member(10L, 2L, "MEMBER", true);
        when(teamMemberMapper.findByTeamIdAndStudentId(10L, 2L)).thenReturn(member);

        UpdateMemberWorkDTO dto = new UpdateMemberWorkDTO();
        dto.setWorkContent("  负责前端页面开发  ");

        service.updateMemberWork(1L, ROLE_STUDENT, 10L, 2L, dto);

        verify(teamMemberMapper).updateWorkContent(10L, 2L, "负责前端页面开发");
    }

    private TeamEntity team(Long id, Long leaderId, String status, Long selectedTopicId, Integer maxSize) {
        TeamEntity team = new TeamEntity();
        team.setId(id);
        team.setTeamName("第一小组");
        team.setLeaderId(leaderId);
        team.setStatus(status);
        team.setSelectedTopicId(selectedTopicId);
        team.setMaxSize(maxSize);
        return team;
    }

    private TeamMemberEntity member(Long teamId, Long studentId, String role, boolean enabled) {
        TeamMemberEntity member = new TeamMemberEntity();
        member.setTeamId(teamId);
        member.setStudentId(studentId);
        member.setMemberRole(role);
        member.setEnabled(enabled);
        return member;
    }

    private TeamJoinRequestEntity request(Long id, Long teamId, Long applicantId, String status) {
        TeamJoinRequestEntity request = new TeamJoinRequestEntity();
        request.setId(id);
        request.setTeamId(teamId);
        request.setApplicantId(applicantId);
        request.setApplyMessage("希望加入");
        request.setStatus(status);
        return request;
    }
}
