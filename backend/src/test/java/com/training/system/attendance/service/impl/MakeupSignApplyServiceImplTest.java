package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.dto.MakeupApplyDTO;
import com.training.system.attendance.dto.MakeupApplyQueryDTO;
import com.training.system.attendance.dto.MakeupReviewDTO;
import com.training.system.attendance.entity.AttendanceRecord;
import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.entity.MakeupSignApply;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.enums.MakeupAuditStatusEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.mapper.AttendanceTaskMapper;
import com.training.system.attendance.mapper.MakeupSignApplyMapper;
import com.training.system.attendance.vo.MakeupApplyVO;
import com.training.system.common.PageResult;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.TeacherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 补签申请 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("补签申请 Service 单元测试")
class MakeupSignApplyServiceImplTest {

    @Mock
    private MakeupSignApplyMapper makeupSignApplyMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private AttendanceTaskMapper attendanceTaskMapper;
    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private MakeupSignApplyServiceImpl service;

    private CurrentUserDTO studentUser;
    private CurrentUserDTO teacherUser;
    private CurrentUserDTO adminUser;

    @BeforeEach
    void setUp() {
        studentUser = new CurrentUserDTO(2L, "STUDENT", 100L);
        teacherUser = new CurrentUserDTO(1L, "TEACHER", 10L);
        adminUser = new CurrentUserDTO(3L, "ADMIN", 1L);
    }

    // ==================== applyMakeup 测试 ====================
    @Nested
    @DisplayName("applyMakeup 方法")
    class ApplyMakeupTests {

        @Test
        @DisplayName("非学生角色提交补签 → 拒绝")
        void shouldRejectNonStudent() {
            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, teacherUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("考勤记录不存在 → 抛异常")
        void shouldThrowWhenRecordNotFound() {
            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);
            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, studentUser));
            assertEquals(404, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("考勤记录状态为正常 → 无需补签")
        void shouldRejectWhenAlreadyNormal() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(1L);
            record.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());

            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(record);

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, studentUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("无需补签"));
        }

        @Test
        @DisplayName("已通过补签的记录 → 拒绝重复申请")
        void shouldRejectWhenAlreadyApproved() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(1L);
            record.setSignStatus(AttendanceSignStatusEnum.MAKEUP.getCode());

            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(record);

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, studentUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("已通过补签"));
        }

        @Test
        @DisplayName("已有待审核的补签申请 → 拒绝重复提交")
        void shouldRejectDuplicatePendingApply() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(1L);
            record.setSignStatus(AttendanceSignStatusEnum.LATE.getCode());

            MakeupSignApply pending = new MakeupSignApply();

            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(record);
            when(makeupSignApplyMapper.selectPendingByTaskAndStudent(1L, 100L)).thenReturn(pending);

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, studentUser));
            assertEquals(409, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("待审核"));
        }

        @Test
        @DisplayName("证明材料路径不合法 → 拒绝提交")
        void shouldRejectInvalidProofFilePath() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(1L);
            record.setSignStatus(AttendanceSignStatusEnum.ABSENT.getCode());

            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(record);
            when(makeupSignApplyMapper.selectPendingByTaskAndStudent(1L, 100L)).thenReturn(null);

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);
            dto.setApplyReason("因病未能及时签到");
            dto.setProofFilePath("../secret.exe");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyMakeup(dto, studentUser));

            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("证明材料"));
            verify(makeupSignApplyMapper, never()).insert(any());
        }

        @Test
        @DisplayName("缺勤学生提交补签 → 成功创建申请")
        void shouldCreateMakeupApplyForAbsentStudent() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(1L);
            record.setSignStatus(AttendanceSignStatusEnum.ABSENT.getCode());

            when(attendanceRecordMapper.selectByTaskAndStudent(1L, 100L)).thenReturn(record);
            when(makeupSignApplyMapper.selectPendingByTaskAndStudent(1L, 100L)).thenReturn(null);
            doAnswer(invocation -> {
                MakeupSignApply a = invocation.getArgument(0);
                a.setApplyId(55L);
                return 1;
            }).when(makeupSignApplyMapper).insert(any(MakeupSignApply.class));

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);
            dto.setApplyReason("因生病未能及时签到");
            dto.setProofFilePath("attendance/证明.pdf");

            Long applyId = service.applyMakeup(dto, studentUser);

            assertNotNull(applyId);
            assertEquals(55L, applyId);
            verify(makeupSignApplyMapper).insert(any(MakeupSignApply.class));
        }

        @Test
        @DisplayName("迟到学生提交补签 → 成功创建申请")
        void shouldCreateMakeupApplyForLateStudent() {
            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(2L);
            record.setSignStatus(AttendanceSignStatusEnum.LATE.getCode());

            when(attendanceRecordMapper.selectByTaskAndStudent(2L, 100L)).thenReturn(record);
            when(makeupSignApplyMapper.selectPendingByTaskAndStudent(2L, 100L)).thenReturn(null);
            doAnswer(invocation -> {
                MakeupSignApply a = invocation.getArgument(0);
                a.setApplyId(56L);
                return 1;
            }).when(makeupSignApplyMapper).insert(any(MakeupSignApply.class));

            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(2L);
            dto.setApplyReason("交通堵塞导致迟到");

            Long applyId = service.applyMakeup(dto, studentUser);

            assertNotNull(applyId);
            assertEquals(56L, applyId);
        }
    }

    // ==================== reviewMakeup 测试 ====================
    @Nested
    @DisplayName("reviewMakeup 方法")
    class ReviewMakeupTests {

        @Test
        @DisplayName("学生角色审核 → 拒绝")
        void shouldRejectStudentReview() {
            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(1L, dto, studentUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("申请不存在 → 抛异常")
        void shouldThrowWhenApplyNotFound() {
            when(makeupSignApplyMapper.selectById(99L)).thenReturn(null);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(99L, dto, teacherUser));
            assertEquals(404, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("申请已审核 → 拒绝重复操作")
        void shouldRejectAlreadyReviewed() {
            MakeupSignApply apply = new MakeupSignApply();
            apply.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(1L, dto, teacherUser));
            assertEquals(409, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("已审核"));
        }

        @Test
        @DisplayName("非本人任务的补签申请 → 拒绝")
        void shouldRejectNotOwnTaskReview() {
            MakeupSignApply apply = new MakeupSignApply();
            apply.setApplyId(1L);
            apply.setTaskId(5L);
            apply.setAuditStatus(MakeupAuditStatusEnum.PENDING.getCode());

            AttendanceTask task = new AttendanceTask();
            task.setTeacherId(999L); // 其他教师

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(5L)).thenReturn(task);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(1L, dto, teacherUser));
            assertEquals(403, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("非法审核结果值 → 参数校验失败")
        void shouldRejectInvalidAuditStatus() {
            MakeupSignApply apply = buildPendingApply(1L, 1L);
            AttendanceTask task = buildTask(teacherUser.getRelatedId());

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(99); // 非法值

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(1L, dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("审核结果"));
        }

        @Test
        @DisplayName("驳回但未填写意见 → 校验失败")
        void shouldRejectRejectionWithoutComment() {
            MakeupSignApply apply = buildPendingApply(1L, 1L);
            AttendanceTask task = buildTask(teacherUser.getRelatedId());

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.REJECTED.getCode());
            dto.setAuditComment(""); // 空意见

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewMakeup(1L, dto, teacherUser));
            assertEquals(400, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("审核意见"));
        }

        @Test
        @DisplayName("审核通过 → 更新申请并更新考勤记录为补签")
        void shouldApproveAndUpdateRecord() {
            MakeupSignApply apply = buildPendingApply(1L, 1L);
            apply.setRecordId(10L);
            apply.setStudentId(100L);
            AttendanceTask task = buildTask(teacherUser.getRelatedId());

            AttendanceRecord record = new AttendanceRecord();
            record.setRecordId(10L);

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);
            when(attendanceRecordMapper.selectById(10L)).thenReturn(record);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());
            dto.setAuditComment("批准补签");

            service.reviewMakeup(1L, dto, teacherUser);

            verify(makeupSignApplyMapper).updateAudit(any(MakeupSignApply.class));
            verify(attendanceRecordMapper).update(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("审核驳回 → 只更新申请，不修改考勤记录")
        void shouldRejectAndNotUpdateRecord() {
            MakeupSignApply apply = buildPendingApply(1L, 1L);
            AttendanceTask task = buildTask(teacherUser.getRelatedId());

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.REJECTED.getCode());
            dto.setAuditComment("证明材料不足，不予通过");

            service.reviewMakeup(1L, dto, teacherUser);

            verify(makeupSignApplyMapper).updateAudit(any(MakeupSignApply.class));
            verify(attendanceRecordMapper, never()).update(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("管理员审核 → 成功")
        void shouldAllowAdminReview() {
            MakeupSignApply apply = buildPendingApply(1L, 1L);
            AttendanceTask task = buildTask(999L); // 不同教师

            when(makeupSignApplyMapper.selectById(1L)).thenReturn(apply);
            when(attendanceTaskMapper.selectById(1L)).thenReturn(task);

            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());
            dto.setAuditComment("管理员批准");

            // 管理员不应抛异常
            assertDoesNotThrow(() -> service.reviewMakeup(1L, dto, adminUser));
            verify(makeupSignApplyMapper).updateAudit(any(MakeupSignApply.class));
        }
    }

    // ==================== page 测试 ====================
    @Nested
    @DisplayName("page 方法")
    class PageTests {

        @Test
        @DisplayName("教师查询补签列表 → 成功")
        void shouldReturnPagedForTeacher() {
            MakeupApplyQueryDTO dto = new MakeupApplyQueryDTO();

            MakeupApplyVO vo = new MakeupApplyVO();
            vo.setApplyId(1L);
            vo.setAuditStatus(MakeupAuditStatusEnum.PENDING.getCode());

            when(makeupSignApplyMapper.countByCondition(any(), any(), any(), any())).thenReturn(1L);
            when(makeupSignApplyMapper.selectPage(any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(vo));

            PageResult<MakeupApplyVO> result = service.page(dto, teacherUser);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("学生查询 → 只返回本人申请")
        void shouldReturnOnlyOwnAppliesForStudent() {
            MakeupApplyQueryDTO dto = new MakeupApplyQueryDTO();

            when(makeupSignApplyMapper.countByCondition(any(), any(), eq(100L), any())).thenReturn(0L);
            when(makeupSignApplyMapper.selectPage(any(), any(), eq(100L), any(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            PageResult<MakeupApplyVO> result = service.page(dto, studentUser);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
        }
    }

    private MakeupSignApply buildPendingApply(Long applyId, Long taskId) {
        MakeupSignApply apply = new MakeupSignApply();
        apply.setApplyId(applyId);
        apply.setTaskId(taskId);
        apply.setAuditStatus(MakeupAuditStatusEnum.PENDING.getCode());
        apply.setStudentId(100L);
        return apply;
    }

    private AttendanceTask buildTask(Long teacherId) {
        AttendanceTask task = new AttendanceTask();
        task.setTaskId(1L);
        task.setTeacherId(teacherId);
        return task;
    }
}
