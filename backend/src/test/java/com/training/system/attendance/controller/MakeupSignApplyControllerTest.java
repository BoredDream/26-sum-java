package com.training.system.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.system.attendance.dto.MakeupApplyDTO;
import com.training.system.attendance.dto.MakeupReviewDTO;
import com.training.system.attendance.enums.MakeupAuditStatusEnum;
import com.training.system.attendance.service.MakeupSignApplyService;
import com.training.system.attendance.vo.MakeupApplyVO;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("补签申请 Controller 测试")
class MakeupSignApplyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MakeupSignApplyService makeupSignApplyService;
    private MockHttpSession studentSession;
    private MockHttpSession teacherSession;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        makeupSignApplyService = mock(MakeupSignApplyService.class);
        // standalone setup with explicit constructor args
        MakeupSignApplyController controller = new MakeupSignApplyController(
                makeupSignApplyService, "/tmp/test-uploads/");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        studentSession = new MockHttpSession();
        studentSession.setAttribute("userId", 2L);
        studentSession.setAttribute("role", "STUDENT");
        studentSession.setAttribute("relatedId", 100L);

        teacherSession = new MockHttpSession();
        teacherSession.setAttribute("userId", 1L);
        teacherSession.setAttribute("role", "TEACHER");
        teacherSession.setAttribute("relatedId", 10L);
    }

    @Nested
    @DisplayName("POST /api/attendance/makeup - 提交补签")
    class Apply {

        @Test
        @DisplayName("缺少必填字段 → 400")
        void shouldReturn400WhenValidationFails() throws Exception {
            MakeupApplyDTO dto = new MakeupApplyDTO();
            mockMvc.perform(post("/api/attendance/makeup")
                            .session(studentSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("正常提交 → 200")
        void shouldReturn200WithApplyId() throws Exception {
            MakeupApplyDTO dto = new MakeupApplyDTO();
            dto.setTaskId(1L);
            dto.setApplyReason("因病未能签到，已附证明");

            when(makeupSignApplyService.applyMakeup(any(), any())).thenReturn(55L);

            mockMvc.perform(post("/api/attendance/makeup")
                            .session(studentSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(55));
        }
    }

    @Nested
    @DisplayName("POST /api/attendance/makeup/{applyId}/review - 审核")
    class Review {

        @Test
        @DisplayName("缺少审核结果 → 400")
        void shouldReturn400WhenNoAuditStatus() throws Exception {
            MakeupReviewDTO dto = new MakeupReviewDTO();
            mockMvc.perform(post("/api/attendance/makeup/1/review")
                            .session(teacherSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("审核通过 → 200")
        void shouldReturn200WhenApproved() throws Exception {
            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.APPROVED.getCode());
            dto.setAuditComment("批准");

            mockMvc.perform(post("/api/attendance/makeup/1/review")
                            .session(teacherSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("审核驳回 → 200")
        void shouldReturn200WhenRejected() throws Exception {
            MakeupReviewDTO dto = new MakeupReviewDTO();
            dto.setAuditStatus(MakeupAuditStatusEnum.REJECTED.getCode());
            dto.setAuditComment("证明材料不足");

            mockMvc.perform(post("/api/attendance/makeup/1/review")
                            .session(teacherSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/attendance/makeup/page - 补签列表")
    class PageApplies {

        @Test
        @DisplayName("正常分页 → 200")
        void shouldReturn200WithPagedData() throws Exception {
            PageResult<MakeupApplyVO> page = new PageResult<>(Collections.emptyList(), 0, 1, 10);
            when(makeupSignApplyService.page(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/attendance/makeup/page").session(teacherSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
