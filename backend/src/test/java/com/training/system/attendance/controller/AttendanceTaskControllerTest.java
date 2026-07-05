package com.training.system.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.training.system.attendance.dto.AttendanceTaskCreateDTO;
import com.training.system.attendance.enums.AttendanceTaskTypeEnum;
import com.training.system.attendance.service.AttendanceTaskService;
import com.training.system.attendance.vo.AttendanceTaskDetailVO;
import com.training.system.attendance.vo.AttendanceTaskVO;
import com.training.system.common.PageResult;
import com.training.system.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("签到任务 Controller 测试")
class AttendanceTaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AttendanceTaskService attendanceTaskService;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        attendanceTaskService = mock(AttendanceTaskService.class);
        AttendanceTaskController controller = new AttendanceTaskController(attendanceTaskService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("role", "TEACHER");
        session.setAttribute("relatedId", 10L);
    }

    @Nested
    @DisplayName("POST /api/attendance/task - 创建任务")
    class CreateTask {

        @Test
        @DisplayName("未登录 → 401")
        void shouldReturn401WhenNotLoggedIn() throws Exception {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            mockMvc.perform(post("/api/attendance/task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("参数校验失败 → 400")
        void shouldReturn400WhenValidationFails() throws Exception {
            AttendanceTaskCreateDTO dto = new AttendanceTaskCreateDTO();
            mockMvc.perform(post("/api/attendance/task")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("正常创建 → 200")
        void shouldReturn200WithTaskId() throws Exception {
            AttendanceTaskCreateDTO dto = buildValidDTO();
            when(attendanceTaskService.createTask(any(), any())).thenReturn(99L);

            mockMvc.perform(post("/api/attendance/task")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(99));
        }
    }

    @Nested
    @DisplayName("GET /api/attendance/task/page - 任务列表")
    class PageTasks {

        @Test
        @DisplayName("正常分页 → 200")
        void shouldReturn200WithPagedData() throws Exception {
            AttendanceTaskVO vo = new AttendanceTaskVO();
            vo.setTaskId(1L);
            vo.setTaskTitle("测试任务");
            PageResult<AttendanceTaskVO> page = new PageResult<>(List.of(vo), 1, 1, 10);
            when(attendanceTaskService.page(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/attendance/task/page")
                            .session(session)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/attendance/task/{taskId} - 任务详情")
    class DetailTask {

        @Test
        @DisplayName("正常查询 → 200")
        void shouldReturn200WithDetail() throws Exception {
            AttendanceTaskDetailVO detail = new AttendanceTaskDetailVO();
            detail.setTaskId(1L);
            detail.setTaskTitle("测试任务");
            when(attendanceTaskService.detail(eq(1L), any())).thenReturn(detail);

            mockMvc.perform(get("/api/attendance/task/1").session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.taskId").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/attendance/task/{taskId}/finish")
    class FinishTask {

        @Test
        @DisplayName("正常结束 → 200")
        void shouldReturn200WhenFinish() throws Exception {
            mockMvc.perform(post("/api/attendance/task/1/finish").session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    private AttendanceTaskCreateDTO buildValidDTO() {
        AttendanceTaskCreateDTO dto = new AttendanceTaskCreateDTO();
        dto.setTaskTitle("每日签到");
        dto.setTaskType(AttendanceTaskTypeEnum.DAILY.getCode());
        dto.setScopeType(3);
        dto.setStartTime(LocalDateTime.now().minusMinutes(10));
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        return dto;
    }
}
