package com.training.system.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.system.attendance.service.AttendanceStatisticsService;
import com.training.system.attendance.vo.AttendanceStatisticsVO;
import com.training.system.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("考勤统计 Controller 测试")
class AttendanceStatisticsControllerTest {

    private MockMvc mockMvc;
    private AttendanceStatisticsService attendanceStatisticsService;
    private MockHttpSession teacherSession;

    @BeforeEach
    void setUp() {
        attendanceStatisticsService = mock(AttendanceStatisticsService.class);
        AttendanceStatisticsController controller = new AttendanceStatisticsController(attendanceStatisticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        teacherSession = new MockHttpSession();
        teacherSession.setAttribute("userId", 1L);
        teacherSession.setAttribute("role", "TEACHER");
        teacherSession.setAttribute("relatedId", 10L);
    }

    @Nested
    @DisplayName("GET /api/attendance/statistics")
    class Statistics {

        @Test
        @DisplayName("未登录 → 401")
        void shouldReturn401WhenNotLoggedIn() throws Exception {
            mockMvc.perform(get("/api/attendance/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("正常查询 → 200")
        void shouldReturn200WithStatistics() throws Exception {
            AttendanceStatisticsVO vo = new AttendanceStatisticsVO();
            vo.setTotalCount(100);
            vo.setNormalCount(80);
            vo.setLateCount(10);
            vo.setAbsentCount(5);
            vo.setMakeupCount(5);
            vo.setAttendanceRate(new BigDecimal("85.00"));

            when(attendanceStatisticsService.statistics(any(), any())).thenReturn(vo);

            mockMvc.perform(get("/api/attendance/statistics").session(teacherSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(100))
                    .andExpect(jsonPath("$.data.attendanceRate").value(85.00));
        }

        @Test
        @DisplayName("带查询条件 → 200")
        void shouldReturn200WithFilters() throws Exception {
            AttendanceStatisticsVO vo = new AttendanceStatisticsVO();
            when(attendanceStatisticsService.statistics(any(), any())).thenReturn(vo);

            mockMvc.perform(get("/api/attendance/statistics")
                            .session(teacherSession)
                            .param("taskId", "1")
                            .param("className", "软件工程2023级1班"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
