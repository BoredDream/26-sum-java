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
import com.training.system.attendance.utils.ScopeValidator;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.common.PageResult;
import com.training.system.exception.BusinessException;
import com.training.system.info.entity.Student;
import com.training.system.info.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 考勤记录 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤记录 Service 单元测试")
class AttendanceRecordServiceImplTest {

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private AttendanceTaskMapper attendanceTaskMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private ScopeValidator scopeValidator;

    @InjectMocks
    private AttendanceRecordServiceImpl service;

    private CurrentUserDTO studentUser;
    private CurrentUserDTO teacherUser;
    private CurrentUserDTO adminUser;
    private AttendanceTask activeTask;

    @BeforeEach
    void setUp() {
        studentUser = new CurrentUserDTO(2L, "STUDENT", 100L);
        teacherUser = new CurrentUserDTO(1L, "TEACHER", 10L);
        adminUser = new CurrentUserDTO(3L, "ADMIN", 1L);

        activeTask = new AttendanceTask();
        activeTask.setTaskId(1L);
        activeTask.setTaskTitle("日常签到");
        activeTask.setStartTime(LocalDateTime.now().minusMinutes(5));
        activeTask.setEndTime(LocalDateTime.now().plusMinutes(30));
        activeTask.setScopeType(3); // 全部学生
        activeTask.setTeacherId(10L);
    }

    // ==================== sign 测试 ====================
    @Nested
    @DisplayName("sign 方法")
    class SignTests {

        @Test
        @DisplayName("非学生角色签到 → 拒绝")
        void shouldRejectNonStudent() {
            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, teacherUser));
            assertEquals(403, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("学生"));
        }

        @Test
        @DisplayName("签到任务不存在 → 抛异常")
        void shouldThrowWhenTaskNotFound() {
            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(99L);
            when(attendanceTaskMapper.selectById(99L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(404, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("学生不在签到范围 → 拒绝")
        void shouldRejectWhenStudentNotInScope() {
            AttendanceTask classTask = new AttendanceTask();
            classTask.setTaskId(1L);
            classTask.setScopeType(1);
            classTask.setScopeValue("软件工程2023级2班");
            classTask.setStartTime(LocalDateTime.now().minusMinutes(5));
            classTask.setEndTime(LocalDateTime.now().plusMinutes(30));

            when(attendanceTaskMapper.selectById(1L)).thenReturn(classTask);
            when(scopeValidator.isStudentInScope(classTask, 100L)).thenReturn(false);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(403, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("范围内"));
        }

        @Test
        @DisplayName("不在签到时间范围内 → 拒绝")
        void shouldRejectWhenOutsideSignTime() {
            AttendanceTask expiredTask = new AttendanceTask();
            expiredTask.setTaskId(1L);
            expiredTask.setStartTime(LocalDateTime.now().plusHours(1));
            expiredTask.setEndTime(LocalDateTime.now().plusHours(2));
            expiredTask.setScopeType(3);

            when(attendanceTaskMapper.selectById(1L)).thenReturn(expiredTask);
            when(scopeValidator.isStudentInScope(expiredTask, 100L)).thenReturn(true);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("签到时间"));
        }

        @Test
        @DisplayName("任务已手动结束 → 拒绝签到")
        void shouldRejectWhenTaskFinished() {
            activeTask.setStatus(AttendanceTaskStatusEnum.FINISHED.getCode());
            when(attendanceTaskMapper.selectById(1L)).thenReturn(activeTask);
            when(scopeValidator.isStudentInScope(activeTask, 100L)).thenReturn(true);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("已结束"));
            verify(attendanceRecordMapper, never()).selectByTaskAndStudent(anyLong(), anyLong());
        }

        @Test
        @DisplayName("考勤记录不存在 → 抛异常")
        void shouldThrowWhenRecordNotFound() {
            when(attendanceTaskMapper.selectById(1L)).thenReturn(activeTask);
            when(scopeValidator.isStudentInScope(activeTask, 100L)).thenReturn(true);
            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(null);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(404, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("考勤记录"));
        }

        @Test
        @DisplayName("重复签到 → 拒绝")
        void shouldRejectDuplicateSign() {
            AttendanceRecord existingRecord = new AttendanceRecord();
            existingRecord.setRecordId(1L);
            existingRecord.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());

            when(attendanceTaskMapper.selectById(1L)).thenReturn(activeTask);
            when(scopeValidator.isStudentInScope(activeTask, 100L)).thenReturn(true);
            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(existingRecord);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.sign(dto, studentUser));
            assertEquals(409, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("正常签到（未迟到）→ 签到成功状态为 NORMAL")
        void shouldSignNormalWhenOnTime() {
            AttendanceRecord freshRecord = new AttendanceRecord();
            freshRecord.setRecordId(1L);
            freshRecord.setTaskId(1L);
            freshRecord.setStudentId(100L);
            freshRecord.setSignStatus(AttendanceSignStatusEnum.ABSENT.getCode());

            when(attendanceTaskMapper.selectById(1L)).thenReturn(activeTask);
            when(scopeValidator.isStudentInScope(activeTask, 100L)).thenReturn(true);
            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(freshRecord);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);
            dto.setRemark("准时签到");

            AttendanceRecordVO result = service.sign(dto, studentUser);

            assertNotNull(result);
            assertEquals(AttendanceSignStatusEnum.NORMAL.getCode(), result.getSignStatus());
            verify(attendanceRecordMapper).update(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("迟到签到 → 签到状态为 LATE")
        void shouldSignLateWhenAfterThreshold() {
            // 创建开始时间已过16分钟的任务（超过15分钟阈值）
            AttendanceTask lateTask = new AttendanceTask();
            lateTask.setTaskId(2L);
            lateTask.setTaskTitle("迟到测试");
            lateTask.setStartTime(LocalDateTime.now().minusMinutes(20));
            lateTask.setEndTime(LocalDateTime.now().plusMinutes(30));
            lateTask.setScopeType(3);
            lateTask.setTeacherId(10L);

            AttendanceRecord freshRecord = new AttendanceRecord();
            freshRecord.setRecordId(2L);
            freshRecord.setTaskId(2L);
            freshRecord.setStudentId(100L);
            freshRecord.setSignStatus(AttendanceSignStatusEnum.ABSENT.getCode());

            when(attendanceTaskMapper.selectById(2L)).thenReturn(lateTask);
            when(scopeValidator.isStudentInScope(lateTask, 100L)).thenReturn(true);
            when(attendanceRecordMapper.selectByTaskAndStudent(2L, 100L)).thenReturn(freshRecord);

            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(2L);

            AttendanceRecordVO result = service.sign(dto, studentUser);

            assertNotNull(result);
            assertEquals(AttendanceSignStatusEnum.LATE.getCode(), result.getSignStatus());
        }
    }

    // ==================== page 测试 ====================
    @Nested
    @DisplayName("page 方法")
    class PageTests {

        @Test
        @DisplayName("教师查询考勤记录 → 成功返回分页数据")
        void shouldReturnPagedRecordsForTeacher() {
            AttendanceRecordQueryDTO dto = new AttendanceRecordQueryDTO();
            dto.setPageNum(1);
            dto.setPageSize(10);

            AttendanceRecordVO vo = new AttendanceRecordVO();
            vo.setRecordId(1L);
            vo.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());

            when(attendanceRecordMapper.countByCondition(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(1L);
            when(attendanceRecordMapper.selectPage(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(vo));

            PageResult<AttendanceRecordVO> result = service.page(dto, teacherUser);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("学生查询 → 强制只查本人记录")
        void shouldForceStudentOnlyOwnRecords() {
            AttendanceRecordQueryDTO dto = new AttendanceRecordQueryDTO();
            dto.setStudentId(999L); // 尝试查他人

            when(attendanceRecordMapper.countByCondition(any(), eq(100L), any(), any(), any(), any(), any()))
                    .thenReturn(0L);
            when(attendanceRecordMapper.selectPage(any(), eq(100L), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            PageResult<AttendanceRecordVO> result = service.page(dto, studentUser);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
        }
    }

    // ==================== listForExport 测试 ====================
    @Nested
    @DisplayName("listForExport 方法")
    class ListForExportTests {

        @Test
        @DisplayName("导出考勤记录 → 成功返回列表")
        void shouldReturnExportList() {
            AttendanceRecordQueryDTO dto = new AttendanceRecordQueryDTO();
            AttendanceRecordVO vo = new AttendanceRecordVO();
            vo.setRecordId(1L);
            vo.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());

            when(attendanceRecordMapper.selectListForExport(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(vo));

            List<AttendanceRecordVO> result = service.listForExport(dto, teacherUser);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("学生导出考勤报表 → 拒绝")
        void shouldRejectStudentExport() {
            AttendanceRecordQueryDTO dto = new AttendanceRecordQueryDTO();

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listForExport(dto, studentUser));

            assertEquals(403, ex.getResultCode().getCode());
            verify(attendanceRecordMapper, never())
                    .selectListForExport(any(), any(), any(), any(), any(), any(), any());
        }
    }

    private Student buildTestStudent() {
        Student student = new Student();
        student.setStudentId(100L);
        student.setStudentNo("2023001");
        student.setStudentName("测试学生");
        student.setClassName("软件工程2023级1班");
        return student;
    }
}
