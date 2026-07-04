package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.AttendanceTaskCreateDTO;
import com.training.system.attendance.dto.AttendanceTaskQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.enums.AttendanceTaskStatusEnum;
import com.training.system.attendance.enums.AttendanceTaskTypeEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.mapper.AttendanceTaskMapper;
import com.training.system.attendance.utils.ScopeValidator;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import com.training.system.common.PageResult;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.info.mapper.TeacherMapper;
import com.training.system.selection.mapper.TeamMemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 签到任务 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("签到任务 Service 单元测试")
class AttendanceTaskServiceImplTest {

    @Mock
    private AttendanceTaskMapper attendanceTaskMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private TeamMemberMapper teamMemberMapper;
    @Mock
    private ScopeValidator scopeValidator;

    @InjectMocks
    private AttendanceTaskServiceImpl service;

    private CurrentUserDTO teacherUser;
    private CurrentUserDTO studentUser;
    private CurrentUserDTO adminUser;

    @BeforeEach
    void setUp() {
        teacherUser = new CurrentUserDTO(1L, "TEACHER", 10L);
        studentUser = new CurrentUserDTO(2L, "STUDENT", 100L);
        adminUser = new CurrentUserDTO(3L, "ADMIN", 1L);
    }

    // ==================== createTask 测试 ====================
    @Nested
    @DisplayName("createTask 方法")
    class CreateTaskTests {

        @Test
        @DisplayName("学生角色发布任务 → 拒绝并抛出权限异常")
        void shouldRejectWhenStudentCreatesTask() {
            AttendanceTaskCreateDTO dto = new AttendanceTaskCreateDTO();
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTask(dto, studentUser));
            assertEquals(403, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("权限"));
        }

        @Test
        @DisplayName("结束时间早于开始时间 → 参数校验失败")
        void shouldRejectWhenEndTimeBeforeStartTime() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setEndTime(LocalDateTime.now());
            dto.setStartTime(LocalDateTime.now().plusHours(1));
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTask(dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("结束时间"));
        }

        @Test
        @DisplayName("scopeType 非法值 → 参数校验失败")
        void shouldRejectInvalidScopeType() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(99);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTask(dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("适用范围"));
        }

        @Test
        @DisplayName("按班级签到未指定班级 → 参数校验失败")
        void shouldRejectClassScopeWithoutValue() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(1); // 班级
            dto.setScopeValue(null);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTask(dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("班级"));
        }

        @Test
        @DisplayName("按团队签到未指定团队 → 参数校验失败")
        void shouldRejectTeamScopeWithoutValue() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(2); // 团队
            dto.setScopeValue("  ");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTask(dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("团队"));
        }

        @Test
        @DisplayName("教师发布全部学生签到 → 成功创建并返回 taskId")
        void shouldCreateTaskForAllStudents() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(3); // 全部
            dto.setScopeValue(null);

            when(studentMapper.selectAllIds()).thenReturn(List.of(100L, 101L, 102L));
            doAnswer(invocation -> {
                AttendanceTask t = invocation.getArgument(0);
                t.setTaskId(99L);
                return 1;
            }).when(attendanceTaskMapper).insert(any(AttendanceTask.class));

            Long taskId = service.createTask(dto, teacherUser);

            assertNotNull(taskId);
            assertEquals(99L, taskId);
            verify(attendanceTaskMapper).insert(any(AttendanceTask.class));
            verify(attendanceRecordMapper, times(3)).insertInitRecord(eq(99L), anyLong());
        }

        @Test
        @DisplayName("教师发布班级签到 → 成功创建")
        void shouldCreateTaskForClass() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(1);
            dto.setScopeValue("软件工程2023级1班");

            when(studentMapper.selectIdsByClassName("软件工程2023级1班"))
                    .thenReturn(List.of(100L, 101L));
            doAnswer(invocation -> {
                AttendanceTask t = invocation.getArgument(0);
                t.setTaskId(88L);
                return 1;
            }).when(attendanceTaskMapper).insert(any(AttendanceTask.class));

            Long taskId = service.createTask(dto, teacherUser);

            assertEquals(88L, taskId);
            verify(attendanceRecordMapper, times(2)).insertInitRecord(eq(88L), anyLong());
        }

        @Test
        @DisplayName("管理员发布任务 → 成功创建")
        void shouldAllowAdminToCreateTask() {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            dto.setScopeType(3);
            when(studentMapper.selectAllIds()).thenReturn(Collections.emptyList());
            doAnswer(invocation -> {
                AttendanceTask t = invocation.getArgument(0);
                t.setTaskId(77L);
                return 1;
            }).when(attendanceTaskMapper).insert(any(AttendanceTask.class));

            Long taskId = service.createTask(dto, adminUser);

            assertNotNull(taskId);
        }
    }

    // ==================== finishTask 测试 ====================
    @Nested
    @DisplayName("finishTask 方法")
    class FinishTaskTests {

        @Test
        @DisplayName("学生尝试结束任务 → 拒绝")
        void shouldRejectWhenStudentFinishesTask() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.finishTask(1L, studentUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("任务不存在 → 抛异常")
        void shouldThrowWhenTaskNotFound() {
            when(attendanceTaskMapper.selectById(99L)).thenReturn(null);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.finishTask(99L, teacherUser));
            assertEquals(404, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("非本人发布的任务 → 拒绝")
        void shouldRejectWhenNotOwnTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTeacherId(999L); // 不是当前教师
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.finishTask(1L, teacherUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("本人发布的任务 → 成功结束")
        void shouldFinishOwnTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTeacherId(teacherUser.getRelatedId());
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            service.finishTask(1L, teacherUser);

            verify(attendanceTaskMapper).updateStatus(1L, AttendanceTaskStatusEnum.FINISHED.getCode());
        }

        @Test
        @DisplayName("管理员结束他人任务 → 成功")
        void shouldAllowAdminToFinishAnyTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTeacherId(999L);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            service.finishTask(1L, adminUser);

            verify(attendanceTaskMapper).updateStatus(1L, AttendanceTaskStatusEnum.FINISHED.getCode());
        }

        @Test
        @DisplayName("任务已结束 → 拒绝重复结束")
        void shouldRejectAlreadyFinishedTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTeacherId(teacherUser.getRelatedId());
            task.setStatus(AttendanceTaskStatusEnum.FINISHED.getCode());
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.finishTask(1L, teacherUser));
            assertEquals(409, ex.getResultCode().getCode());
        }
    }

    // ==================== page 测试 ====================
    @Nested
    @DisplayName("page 方法")
    class PageTests {

        @Test
        @DisplayName("教师查询任务列表 → 成功返回分页数据")
        void shouldReturnPagedTasksForTeacher() {
            AttendanceTaskQueryDTO dto = new AttendanceTaskQueryDTO();
            dto.setPageNum(1);
            dto.setPageSize(10);

            AttendanceTaskVO vo = new AttendanceTaskVO();
            vo.setTaskId(1L);
            vo.setTeacherId(teacherUser.getRelatedId());
            vo.setTaskType(AttendanceTaskTypeEnum.DAILY.getCode());
            vo.setScopeType(1);

            when(attendanceTaskMapper.countByCondition(any(), any(), any(), any())).thenReturn(1L);
            when(attendanceTaskMapper.selectPage(any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(vo));
            when(teacherMapper.selectTeacherNamesByIds(anyList())).thenReturn(
                    List.of(Map.of("teacher_id", teacherUser.getRelatedId(), "teacher_name", "张老师")));

            PageResult<AttendanceTaskVO> result = service.page(dto, teacherUser);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
            verify(teacherMapper).selectTeacherNamesByIds(anyList());
        }

        @Test
        @DisplayName("学生查询 → 按学生过滤")
        void shouldFilterByStudent() {
            AttendanceTaskQueryDTO dto = new AttendanceTaskQueryDTO();

            when(attendanceTaskMapper.countByCondition(any(), any(), any(), any())).thenReturn(0L);
            when(attendanceTaskMapper.selectPage(any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            PageResult<AttendanceTaskVO> result = service.page(dto, studentUser);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
        }
    }

    // ==================== detail 测试 ====================
    @Nested
    @DisplayName("detail 方法")
    class DetailTests {

        @Test
        @DisplayName("任务不存在 → 抛异常")
        void shouldThrowWhenTaskNotFound() {
            when(attendanceTaskMapper.selectById(99L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.detail(99L, teacherUser));
            assertEquals(404, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("教师查看本人任务详情 → 成功返回")
        void shouldReturnDetailForOwnTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTaskId(1L);
            task.setTeacherId(teacherUser.getRelatedId());
            task.setScopeType(3);

            AttendanceTaskDetailVO detailVO = new AttendanceTaskDetailVO();
            detailVO.setTaskId(1L);
            detailVO.setTeacherId(teacherUser.getRelatedId());
            detailVO.setTaskType(AttendanceTaskTypeEnum.DAILY.getCode());
            detailVO.setScopeType(3);

            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);
            when(attendanceTaskMapper.selectDetailById(1L)).thenReturn(detailVO);
            when(attendanceRecordMapper.selectPage(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(teacherMapper.selectTeacherNamesByIds(anyList())).thenReturn(
                    List.of(Map.of("teacher_id", teacherUser.getRelatedId(), "teacher_name", "张老师")));

            AttendanceTaskDetailVO result = service.detail(1L, teacherUser);

            assertNotNull(result);
            assertEquals(1L, result.getTaskId());
        }

        @Test
        @DisplayName("教师查看他人任务 → 拒绝")
        void shouldRejectWhenNotOwnTask() {
            AttendanceTask task = new AttendanceTask();
            task.setTaskId(1L);
            task.setTeacherId(999L); // 其他教师
            task.setScopeType(1);

            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.detail(1L, teacherUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("学生在签到范围内查看 → 成功返回")
        void shouldAllowStudentInScope() {
            AttendanceTask task = new AttendanceTask();
            task.setTaskId(1L);
            task.setTeacherId(10L);
            task.setScopeType(3); // 全部学生

            AttendanceTaskDetailVO detailVO = new AttendanceTaskDetailVO();
            detailVO.setTaskId(1L);
            detailVO.setTeacherId(10L);
            detailVO.setTaskType(AttendanceTaskTypeEnum.DAILY.getCode());
            detailVO.setScopeType(3);

            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);
            when(scopeValidator.isStudentInScope(task, 100L)).thenReturn(true);
            when(attendanceTaskMapper.selectDetailById(1L)).thenReturn(detailVO);
            when(attendanceRecordMapper.selectPage(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(teacherMapper.selectTeacherNamesByIds(anyList())).thenReturn(
                    List.of(Map.of("teacher_id", 10L, "teacher_name", "李老师")));

            AttendanceTaskDetailVO result = service.detail(1L, studentUser);

            assertNotNull(result);
        }
    }

    private AttendanceTaskCreateDTO buildValidDTO() {
        AttendanceTaskCreateDTO dto = new AttendanceTaskCreateDTO();
        dto.setTaskTitle("每日签到");
        dto.setTaskType(AttendanceTaskTypeEnum.DAILY.getCode());
        dto.setScopeType(3);
        dto.setStartTime(LocalDateTime.now().minusMinutes(10));
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        dto.setDescription("测试签到任务");
        return dto;
    }
}
