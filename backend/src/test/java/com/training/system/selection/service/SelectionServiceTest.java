package com.training.system.selection.service;

import com.training.system.exception.BusinessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.training.system.selection.service.SelectionConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectionServiceTest {
    @Mock
    private TeamService teamService;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private TopicMapper topicMapper;
    @Mock
    private TopicSelectionMapper topicSelectionMapper;

    @Captor
    private ArgumentCaptor<TopicSelectionEntity> selectionCaptor;
    @Captor
    private ArgumentCaptor<TeamEntity> teamCaptor;

    private SelectionService service;

    @BeforeEach
    void setUp() {
        service = new SelectionService(teamService, teamMapper, teamMemberMapper, topicMapper, topicSelectionMapper);
    }

    @Test
    @DisplayName("开放课题列表 - 关键词会去除首尾空格")
    void listOpenTopics_trimsKeyword() {
        when(topicMapper.findOpenTopics("商城")).thenReturn(List.of(topic(20L, 8L, TOPIC_OPEN, 1, 5)));

        assertEquals(1, service.listOpenTopics("  商城  ").size());

        verify(topicMapper).findOpenTopics("商城");
    }

    @Test
    @DisplayName("提交选题 - 团队负责人可提交且选题理由会 trim")
    void submitSelection_success() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(3);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("  项目方向匹配  ");

        SelectionVO result = service.submitSelection(1L, ROLE_STUDENT, dto);

        assertEquals(10L, result.getTeamId());
        assertEquals(20L, result.getTopicId());
        verify(topicSelectionMapper).insert(selectionCaptor.capture());
        assertEquals(SELECTION_PENDING, selectionCaptor.getValue().getStatus());
        assertEquals("项目方向匹配", selectionCaptor.getValue().getSelectionReason());
    }

    @Test
    @DisplayName("提交选题 - 已有待审核申请时禁止重复提交")
    void submitSelection_pendingExistsThrows() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(selection(99L, 10L, 20L, SELECTION_PENDING));

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitSelection(1L, ROLE_STUDENT, dto));

        assertTrue(ex.getMessage().contains("待审核"));
        verify(topicSelectionMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交选题 - 团队人数不符合课题要求时拒绝")
    void submitSelection_teamSizeInvalidThrows() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic(20L, 8L, TOPIC_OPEN, 3, 5));
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(2);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitSelection(1L, ROLE_STUDENT, dto));

        assertTrue(ex.getMessage().contains("不符合课题要求"));
        verify(topicSelectionMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交选题 - 未到选题开始时间时拒绝")
    void submitSelection_beforeStartThrows() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 1, 5);
        topic.setSelectionStart(LocalDateTime.now().plusDays(1));
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitSelection(1L, ROLE_STUDENT, dto));

        assertTrue(ex.getMessage().contains("尚未开始"));
        verify(topicSelectionMapper, never()).insert(any());
    }

    @Test
    @DisplayName("审核选题 - 负责教师审核通过后团队变为已选题")
    void auditSelection_teacherApproveSuccess() {
        TopicSelectionEntity selection = selection(99L, 10L, 20L, SELECTION_PENDING);
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        doNothing().when(teamService).requireTeacherOrAdmin(ROLE_TEACHER);
        when(topicSelectionMapper.findById(99L)).thenReturn(selection);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamService.getTeamById(10L)).thenReturn(team);
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(3);

        AuditSelectionDTO dto = new AuditSelectionDTO();
        dto.setApproved(true);
        dto.setOpinion("同意");

        SelectionVO result = service.auditSelection(8L, ROLE_TEACHER, 99L, dto);

        assertEquals(SELECTION_APPROVED, result.getStatus());
        verify(teamMapper).updateSelectionStatus(teamCaptor.capture());
        assertEquals(TEAM_SELECTED, teamCaptor.getValue().getStatus());
        assertEquals(20L, teamCaptor.getValue().getSelectedTopicId());
        verify(topicSelectionMapper).audit(selectionCaptor.capture());
        assertEquals(8L, selectionCaptor.getValue().getAuditTeacherId());
    }

    @Test
    @DisplayName("审核选题 - 教师不能审核其他教师课题")
    void auditSelection_otherTeacherForbidden() {
        doNothing().when(teamService).requireTeacherOrAdmin(ROLE_TEACHER);
        when(topicSelectionMapper.findById(99L)).thenReturn(selection(99L, 10L, 20L, SELECTION_PENDING));
        when(topicMapper.findById(20L)).thenReturn(topic(20L, 8L, TOPIC_OPEN, 1, 5));

        AuditSelectionDTO dto = new AuditSelectionDTO();
        dto.setApproved(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.auditSelection(9L, ROLE_TEACHER, 99L, dto));

        assertTrue(ex.getMessage().contains("本人发布"));
        verify(teamMapper, never()).updateSelectionStatus(any());
        verify(topicSelectionMapper, never()).audit(any());
    }

    @Test
    @DisplayName("审核选题 - 驳回时必须填写审核意见")
    void auditSelection_rejectWithoutOpinionThrows() {
        doNothing().when(teamService).requireTeacherOrAdmin(ROLE_ADMIN);
        when(topicSelectionMapper.findById(99L)).thenReturn(selection(99L, 10L, 20L, SELECTION_PENDING));

        AuditSelectionDTO dto = new AuditSelectionDTO();
        dto.setApproved(false);
        dto.setOpinion(" ");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.auditSelection(1L, ROLE_ADMIN, 99L, dto));

        assertTrue(ex.getMessage().contains("审核意见"));
        verify(topicSelectionMapper, never()).audit(any());
    }

    @Test
    @DisplayName("待审核选题 - 管理员查全部，教师只查本人课题")
    void getPendingSelections_filtersByRole() {
        doNothing().when(teamService).requireTeacherOrAdmin(anyString());
        when(topicSelectionMapper.findAllPending()).thenReturn(List.of(new SelectionVO()));
        when(topicSelectionMapper.findPendingByTeacherId(8L)).thenReturn(List.of(new SelectionVO(), new SelectionVO()));

        assertEquals(1, service.getPendingSelections(1L, ROLE_ADMIN).size());
        assertEquals(2, service.getPendingSelections(8L, ROLE_TEACHER).size());

        verify(topicSelectionMapper).findAllPending();
        verify(topicSelectionMapper).findPendingByTeacherId(8L);
    }

    @Test
    @DisplayName("课题详情 - 返回课题信息")
    void getTopicDetail_success() {
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        when(topicMapper.findById(20L)).thenReturn(topic);

        com.training.system.selection.vo.TopicVO result = service.getTopicDetail(20L);

        assertEquals(20L, result.getId());
        assertEquals("在线商城系统", result.getTitle());
    }

    @Test
    @DisplayName("我的选题 - 学生查看团队选题列表")
    void getMySelections_success() {
        TeamEntity team = team(10L, 1L, TEAM_SELECTED, 20L);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        SelectionVO vo = new SelectionVO();
        vo.setSelectionId(99L);
        when(topicSelectionMapper.findViewByTeamId(10L)).thenReturn(List.of(vo));

        List<SelectionVO> result = service.getMySelections(1L, ROLE_STUDENT);

        assertEquals(1, result.size());
        assertEquals(99L, result.get(0).getSelectionId());
    }

    @Test
    @DisplayName("审核选题 - 教师驳回申请")
    void auditSelection_rejectSuccess() {
        TopicSelectionEntity selection = selection(99L, 10L, 20L, SELECTION_PENDING);
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        doNothing().when(teamService).requireTeacherOrAdmin(ROLE_TEACHER);
        when(topicSelectionMapper.findById(99L)).thenReturn(selection);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamService.getTeamById(10L)).thenReturn(team);

        AuditSelectionDTO dto = new AuditSelectionDTO();
        dto.setApproved(false);
        dto.setOpinion("课题方向不符");

        SelectionVO result = service.auditSelection(8L, ROLE_TEACHER, 99L, dto);

        assertEquals(SELECTION_REJECTED, result.getStatus());
        assertEquals("课题方向不符", result.getAuditOpinion());
        verify(teamMapper, never()).updateSelectionStatus(any());
        verify(topicSelectionMapper).audit(selectionCaptor.capture());
        assertEquals(8L, selectionCaptor.getValue().getAuditTeacherId());
    }

    @Test
    @DisplayName("提交选题 - 刚好到达开始时间允许提交")
    void submitSelection_atStartTimeAllowed() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        topic.setSelectionStart(LocalDateTime.now());
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(3);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        SelectionVO result = service.submitSelection(1L, ROLE_STUDENT, dto);

        assertEquals(SELECTION_PENDING, result.getStatus());
        verify(topicSelectionMapper).insert(selectionCaptor.capture());
    }

    @Test
    @DisplayName("提交选题 - 超过结束时间拒绝提交")
    void submitSelection_afterEndTimeThrows() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 2, 5);
        topic.setSelectionEnd(LocalDateTime.now().minusMinutes(1));
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitSelection(1L, ROLE_STUDENT, dto));

        assertTrue(ex.getMessage().contains("选题时间已结束"));
    }

    @Test
    @DisplayName("提交选题 - 团队人数刚好满足下限允许提交")
    void submitSelection_teamSizeAtMinAllowed() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 3, 5);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(3);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        SelectionVO result = service.submitSelection(1L, ROLE_STUDENT, dto);

        assertEquals(SELECTION_PENDING, result.getStatus());
    }

    @Test
    @DisplayName("提交选题 - 团队人数超过上限拒绝提交")
    void submitSelection_teamSizeAboveMaxThrows() {
        TeamEntity team = team(10L, 1L, TEAM_BUILDING, null);
        TopicEntity topic = topic(20L, 8L, TOPIC_OPEN, 3, 5);
        doNothing().when(teamService).requireStudent(ROLE_STUDENT);
        when(teamService.getCurrentTeamByStudent(1L)).thenReturn(team);
        doNothing().when(teamService).assertTeamLeader(1L, ROLE_STUDENT, 10L);
        when(topicSelectionMapper.findPendingByTeamId(10L)).thenReturn(null);
        when(topicMapper.findById(20L)).thenReturn(topic);
        when(teamMemberMapper.countActiveByTeamId(10L)).thenReturn(6);

        SubmitSelectionDTO dto = new SubmitSelectionDTO();
        dto.setTopicId(20L);
        dto.setSelectionReason("申请选题");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.submitSelection(1L, ROLE_STUDENT, dto));

        assertTrue(ex.getMessage().contains("不符合课题要求"));
    }

    private TeamEntity team(Long id, Long leaderId, String status, Long selectedTopicId) {
        TeamEntity team = new TeamEntity();
        team.setId(id);
        team.setTeamName("第一小组");
        team.setLeaderId(leaderId);
        team.setStatus(status);
        team.setSelectedTopicId(selectedTopicId);
        return team;
    }

    private TopicEntity topic(Long id, Long teacherId, String status, Integer minMembers, Integer maxMembers) {
        TopicEntity topic = new TopicEntity();
        topic.setId(id);
        topic.setTitle("在线商城系统");
        topic.setTeacherId(teacherId);
        topic.setStatus(status);
        topic.setMinMembers(minMembers);
        topic.setMaxMembers(maxMembers);
        return topic;
    }

    private TopicSelectionEntity selection(Long id, Long teamId, Long topicId, String status) {
        TopicSelectionEntity selection = new TopicSelectionEntity();
        selection.setId(id);
        selection.setTeamId(teamId);
        selection.setTopicId(topicId);
        selection.setSelectionReason("申请选题");
        selection.setStatus(status);
        return selection;
    }
}
