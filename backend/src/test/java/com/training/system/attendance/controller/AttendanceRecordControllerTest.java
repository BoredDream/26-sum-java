package com.training.system.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.system.attendance.dto.AttendanceSignDTO;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.service.AttendanceRecordService;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.common.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("考勤记录 Controller 测试")
class AttendanceRecordControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AttendanceRecordService attendanceRecordService;
    private MockHttpSession session;
    private MockHttpSession teacherSession;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 LocalDateTime 序列化
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        attendanceRecordService = mock(AttendanceRecordService.class);
        AttendanceRecordController controller = new AttendanceRecordController(attendanceRecordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.training.system.exception.GlobalExceptionHandler())
                .build();

        session = new MockHttpSession();
        session.setAttribute("userId", 2L);
        session.setAttribute("role", "STUDENT");
        session.setAttribute("relatedId", 100L);

        teacherSession = new MockHttpSession();
        teacherSession.setAttribute("userId", 1L);
        teacherSession.setAttribute("role", "TEACHER");
        teacherSession.setAttribute("relatedId", 10L);
    }

    @Nested
    @DisplayName("POST /api/attendance/record/sign")
    class Sign {

        @Test
        @DisplayName("未登录 → 401")
        void shouldReturn401WhenNotLoggedIn() throws Exception {
            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);
            mockMvc.perform(post("/api/attendance/record/sign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("缺少 taskId → 400")
        void shouldReturn400WhenNoTaskId() throws Exception {
            AttendanceSignDTO dto = new AttendanceSignDTO();
            mockMvc.perform(post("/api/attendance/record/sign")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("正常签到 → 200")
        void shouldReturn200WithRecord() throws Exception {
            AttendanceSignDTO dto = new AttendanceSignDTO();
            dto.setTaskId(1L);

            AttendanceRecordVO vo = new AttendanceRecordVO();
            vo.setRecordId(1L);
            vo.setSignStatus(AttendanceSignStatusEnum.NORMAL.getCode());
            vo.setSignTime(LocalDateTime.now());

            when(attendanceRecordService.sign(any(), any())).thenReturn(vo);

            mockMvc.perform(post("/api/attendance/record/sign")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.signStatus").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/attendance/record/page")
    class PageRecords {

        @Test
        @DisplayName("未登录 → 401")
        void shouldReturn401WhenNotLoggedIn() throws Exception {
            mockMvc.perform(get("/api/attendance/record/page"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("正常分页 → 200")
        void shouldReturn200WithPagedData() throws Exception {
            PageResult<AttendanceRecordVO> page = new PageResult<>(Collections.emptyList(), 0, 1, 10);
            when(attendanceRecordService.page(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/attendance/record/page").session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/attendance/export")
    class Export {

        @Test
        @DisplayName("正常导出 → 返回 xlsx")
        void shouldReturnExcelFile() throws Exception {
            AttendanceRecordVO vo = new AttendanceRecordVO();
            vo.setTaskTitle("每日签到");
            vo.setStudentName("张三");
            vo.setStudentNo("S001");
            vo.setClassName("软件工程2023级1班");
            vo.setSignStatusName("正常");
            vo.setIsMakeup(0);

            when(attendanceRecordService.listForExport(any(), any())).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/attendance/export").session(teacherSession))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("attachment")))
                    .andExpect(content().contentType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }

        @Test
        @DisplayName("兼容旧导出路径 → 返回 xlsx")
        void shouldKeepOldExportPath() throws Exception {
            when(attendanceRecordService.listForExport(any(), any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/attendance/record/export").session(teacherSession))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }
    }
}
