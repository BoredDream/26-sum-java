package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.AttendanceStatisticsQueryDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.attendance.vo.AttendanceStatisticsVO;
import com.training.system.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 考勤统计 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤统计 Service 单元测试")
class AttendanceStatisticsServiceImplTest {

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    @InjectMocks
    private AttendanceStatisticsServiceImpl service;

    private CurrentUserDTO teacherUser;
    private CurrentUserDTO studentUser;
    private CurrentUserDTO adminUser;

    @BeforeEach
    void setUp() {
        teacherUser = new CurrentUserDTO(1L, "TEACHER", 10L);
        studentUser = new CurrentUserDTO(2L, "STUDENT", 100L);
        adminUser = new CurrentUserDTO(3L, "ADMIN", 1L);
    }

    @Nested
    @DisplayName("statistics 方法")
    class StatisticsTests {

        @Test
        @DisplayName("学生角色查询统计 → 拒绝")
        void shouldRejectStudent() {
            AttendanceStatisticsQueryDTO dto = new AttendanceStatisticsQueryDTO();
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.statistics(dto, studentUser));
            assertEquals(403, ex.getResultCode().getCode());
            assertTrue(ex.getMessage().contains("权限"));
        }

        @Test
        @DisplayName("教师查询统计 → 成功返回统计结果")
        void shouldReturnStatisticsForTeacher() {
            List<AttendanceRecordVO> records = buildTestRecords();
            when(attendanceRecordMapper.selectListForStatistics(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(records);

            AttendanceStatisticsQueryDTO dto = new AttendanceStatisticsQueryDTO();
            AttendanceStatisticsVO result = service.statistics(dto, teacherUser);

            assertNotNull(result);
            assertEquals(6, result.getTotalCount());
            assertEquals(3, result.getNormalCount());
            assertEquals(1, result.getLateCount());
            assertEquals(1, result.getAbsentCount());
            assertEquals(1, result.getMakeupCount());
        }

        @Test
        @DisplayName("管理员查询统计 → 成功")
        void shouldReturnStatisticsForAdmin() {
            when(attendanceRecordMapper.selectListForStatistics(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(buildTestRecords());

            AttendanceStatisticsQueryDTO dto = new AttendanceStatisticsQueryDTO();
            AttendanceStatisticsVO result = service.statistics(dto, adminUser);

            assertNotNull(result);
            assertEquals(6, result.getTotalCount());
        }

        @Test
        @DisplayName("无记录 → 全为零且出勤率 0%")
        void shouldReturnZeroWhenNoRecords() {
            when(attendanceRecordMapper.selectListForStatistics(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new ArrayList<>());

            AttendanceStatisticsQueryDTO dto = new AttendanceStatisticsQueryDTO();
            AttendanceStatisticsVO result = service.statistics(dto, teacherUser);

            assertNotNull(result);
            assertEquals(0, result.getTotalCount());
            assertEquals(0, result.getNormalCount());
            assertEquals(BigDecimal.ZERO, result.getAttendanceRate());
        }

        @Test
        @DisplayName("全部正常签到 → 出勤率 100%")
        void shouldReturn100PercentWhenAllNormal() {
            List<AttendanceRecordVO> records = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                AttendanceRecordVO vo = new AttendanceRecordVO();
                vo.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());
                records.add(vo);
            }
            when(attendanceRecordMapper.selectListForStatistics(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(records);

            AttendanceStatisticsQueryDTO dto = new AttendanceStatisticsQueryDTO();
            AttendanceStatisticsVO result = service.statistics(dto, teacherUser);

            assertEquals(new BigDecimal("100.00"), result.getAttendanceRate());
        }

        @Test
        @DisplayName("出勤率计算（正常+补签视为出勤）→ 验证计算公式")
        void shouldCalculateCorrectAttendanceRate() {
            List<AttendanceRecordVO> records = new ArrayList<>();
            // 2 normal, 1 makeup, 1 absent → (2+1)/4 = 75%
            records.add(buildRecord(AttendanceSignStatusEnum.NORMAL.getCode()));
            records.add(buildRecord(AttendanceSignStatusEnum.NORMAL.getCode()));
            records.add(buildRecord(AttendanceSignStatusEnum.MAKEUP.getCode()));
            records.add(buildRecord(AttendanceSignStatusEnum.ABSENT.getCode()));

            when(attendanceRecordMapper.selectListForStatistics(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(records);

            AttendanceStatisticsVO result = service.statistics(new AttendanceStatisticsQueryDTO(), teacherUser);

            assertEquals(4, result.getTotalCount());
            assertEquals(new BigDecimal("75.00"), result.getAttendanceRate());
        }
    }

    private List<AttendanceRecordVO> buildTestRecords() {
        List<AttendanceRecordVO> records = new ArrayList<>();
        // 3 正常
        for (int i = 0; i < 3; i++) {
            records.add(buildRecord(AttendanceSignStatusEnum.NORMAL.getCode()));
        }
        // 1 迟到
        records.add(buildRecord(AttendanceSignStatusEnum.LATE.getCode()));
        // 1 缺勤
        records.add(buildRecord(AttendanceSignStatusEnum.ABSENT.getCode()));
        // 1 补签
        records.add(buildRecord(AttendanceSignStatusEnum.MAKEUP.getCode()));
        return records;
    }

    private AttendanceRecordVO buildRecord(Integer signStatus) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setSignStatus(signStatus);
        return vo;
    }
}
