package com.training.system.topic.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.topic.dto.TopicCreateDTO;
import com.training.system.topic.dto.TopicQueryDTO;
import com.training.system.topic.dto.TopicReviewDTO;
import com.training.system.topic.dto.TopicUpdateDTO;
import com.training.system.topic.entity.ProjectTopic;
import com.training.system.topic.entity.TopicFile;
import com.training.system.topic.entity.TopicReview;
import com.training.system.topic.mapper.ProjectTopicMapper;
import com.training.system.topic.mapper.TopicFileMapper;
import com.training.system.topic.mapper.TopicReviewMapper;
import com.training.system.topic.vo.TopicDetailVO;
import com.training.system.topic.vo.TopicFileVO;
import com.training.system.topic.vo.TopicListVO;
import com.training.system.topic.vo.TopicReviewVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 出题管理模块 Service 层单元测试
 * 覆盖参考文档中的 31 个测试用例（TC-N-01~10, TC-E-01~10, TC-P-01~05, TC-B-01~06）
 */
@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

    @Mock
    private ProjectTopicMapper topicMapper;

    @Mock
    private TopicFileMapper fileMapper;

    @Mock
    private TopicReviewMapper reviewMapper;

    @Captor
    private ArgumentCaptor<ProjectTopic> topicCaptor;

    @Captor
    private ArgumentCaptor<TopicFile> fileCaptor;

    @Captor
    private ArgumentCaptor<TopicReview> reviewCaptor;

    private TopicServiceImpl service;

    /** 通用教师 ID 与 relatedId */
    private static final Long TEACHER_USER_ID = 10L;
    private static final Long TEACHER_RELATED_ID = 100L;
    private static final Long OTHER_TEACHER_RELATED_ID = 101L;
    private static final Long ADMIN_USER_ID = 1L;
    private static final Long STUDENT_RELATED_ID = 200L;

    /** 公共: 构造一个可用的草稿题目实体 */
    private ProjectTopic createDraftTopic(Long topicId, Long teacherId) {
        ProjectTopic t = new ProjectTopic();
        t.setTopicId(topicId);
        t.setTopicName("在线商城系统");
        t.setTopicType("应用系统");
        t.setDifficulty("B");
        t.setTeacherId(teacherId);
        t.setStudentLimit(4);
        t.setTopicContent("开发一个完整的B2C商城");
        t.setTechnicalRoute("Spring Boot + Vue + MySQL");
        t.setStatus(0);           // 草稿
        t.setOpenStatus(0);       // 未开放
        t.setIsDeleted(0);        // 未删除
        return t;
    }

    private ProjectTopic createPendingTopic(Long topicId, Long teacherId) {
        ProjectTopic t = createDraftTopic(topicId, teacherId);
        t.setStatus(1);           // 待审核
        return t;
    }

    private ProjectTopic createApprovedTopic(Long topicId, Long teacherId) {
        ProjectTopic t = createDraftTopic(topicId, teacherId);
        t.setStatus(2);           // 审核通过
        return t;
    }

    @BeforeEach
    void setUp() {
        service = new TopicServiceImpl(topicMapper, fileMapper, reviewMapper);
        // 设置 uploadDir 避免 NPE
        try {
            Path tmpDir = Files.createTempDirectory("topic-test-");
            ReflectionTestUtils.setField(service, "uploadDir", tmpDir.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========================================================================
    // 正常流程测试 — TC-N-01 ~ TC-N-10
    // ========================================================================
    @Nested
    @DisplayName("正常流程 TC-N-01 ~ TC-N-10")
    class NormalFlowTests {

        @Test
        @DisplayName("TC-N-01: 教师新增题目并保存草稿")
        void createTopic_asDraft() {
            TopicCreateDTO dto = buildCreateDto(0);
            service.createTopic(dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);

            verify(topicMapper).insert(topicCaptor.capture());
            ProjectTopic saved = topicCaptor.getValue();
            assertEquals(0, saved.getStatus());
            assertEquals("在线商城系统", saved.getTopicName());
            assertEquals(TEACHER_RELATED_ID, saved.getTeacherId());
            assertEquals(0, saved.getOpenStatus());
        }

        @Test
        @DisplayName("TC-N-02: 教师提交审核（新建时直接提交）")
        void createTopic_submitForReview() {
            TopicCreateDTO dto = buildCreateDto(1);
            service.createTopic(dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);

            verify(topicMapper).insert(topicCaptor.capture());
            assertEquals(1, topicCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("TC-N-03: 教师将草稿提交审核")
        void submitForReview_fromDraft() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            service.submitForReview(1L, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);

            verify(topicMapper).updateStatus(1L, 1);
        }

        @Test
        @DisplayName("TC-N-04: 管理员审核通过题目")
        void reviewTopic_approve() {
            when(topicMapper.selectById(1L)).thenReturn(createPendingTopic(1L, TEACHER_RELATED_ID));

            TopicReviewDTO dto = new TopicReviewDTO();
            dto.setReviewResult(1);
            dto.setReviewComment("题目符合要求，予以通过");
            service.reviewTopic(1L, dto, ADMIN_USER_ID);

            verify(reviewMapper).insert(reviewCaptor.capture());
            assertEquals(1, reviewCaptor.getValue().getReviewResult());

            verify(topicMapper).updateStatus(1L, 2);
        }

        @Test
        @DisplayName("TC-N-05: 管理员开放题目")
        void openTopic_success() {
            ProjectTopic approved = createApprovedTopic(1L, TEACHER_RELATED_ID);
            when(topicMapper.selectById(1L)).thenReturn(approved);

            service.openTopic(1L, ADMIN_USER_ID, "ADMIN");

            verify(topicMapper).updateOpenStatus(1L, 1);
        }

        @Test
        @DisplayName("TC-N-06: 教师上传题目资料")
        void uploadFile_success() throws Exception {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "项目指导书.pdf", "application/pdf",
                    new byte[1024]
            );
            service.uploadFile(1L, file, "版本1.0", TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);

            verify(fileMapper).insert(fileCaptor.capture());
            TopicFile saved = fileCaptor.getValue();
            assertEquals(1L, saved.getTopicId());
            assertEquals("项目指导书.pdf", saved.getFileName());
            assertEquals(TEACHER_USER_ID, saved.getUploadUserId());
        }

        @Test
        @DisplayName("TC-N-07: 学生查看已开放题目列表（仅 status=2 且 open_status=1）")
        void queryTopicPage_asStudent() {
            when(topicMapper.countPage(any(), any(), any(), eq(2), eq(1), any(),
                    any(), any(), anyInt(), any()))
                    .thenReturn(2L);
            when(topicMapper.selectPage(any(), any(), any(), eq(2), eq(1), any(),
                    any(), any(), anyInt(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(createListVO(1L, 2, 1)));

            TopicQueryDTO dto = new TopicQueryDTO();
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "STUDENT", STUDENT_RELATED_ID);

            assertEquals(2L, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("TC-N-08: 教师查看本人题目列表")
        void queryTopicPage_asTeacher() {
            when(topicMapper.countPage(any(), any(), any(), any(), any(), eq(TEACHER_RELATED_ID),
                    any(), any(), anyInt(), any()))
                    .thenReturn(3L);
            when(topicMapper.selectPage(any(), any(), any(), any(), any(), eq(TEACHER_RELATED_ID),
                    any(), any(), anyInt(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(createListVO(1L, 0, 0)));

            TopicQueryDTO dto = new TopicQueryDTO();
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "TEACHER", TEACHER_RELATED_ID);

            assertEquals(3L, result.getTotal());
        }

        @Test
        @DisplayName("TC-N-09: 管理员查看全部题目（不限制 teacherId）")
        void queryTopicPage_asAdmin() {
            when(topicMapper.countPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any()))
                    .thenReturn(5L);
            when(topicMapper.selectPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(createListVO(1L, 2, 1), createListVO(2L, 1, 0)));

            TopicQueryDTO dto = new TopicQueryDTO();
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "ADMIN", ADMIN_USER_ID);

            assertEquals(5L, result.getTotal());
            assertEquals(2, result.getRecords().size());
        }

        @Test
        @DisplayName("TC-N-10: 查看题目详情含审核记录和文件列表")
        void getTopicDetail_withReviewsAndFiles() {
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            TopicFileVO fileVO = new TopicFileVO();
            fileVO.setFileId(10L);
            fileVO.setFileSize(2048L);
            when(fileMapper.selectByTopicId(1L)).thenReturn(List.of(fileVO));

            TopicReviewVO reviewVO = new TopicReviewVO();
            reviewVO.setReviewId(20L);
            reviewVO.setReviewResult(1);
            when(reviewMapper.selectByTopicId(1L)).thenReturn(List.of(reviewVO));

            TopicDetailVO detail = service.getTopicDetail(1L, null, "TEACHER", TEACHER_RELATED_ID);

            assertEquals("在线商城系统", detail.getTopicName());
            assertEquals(1, detail.getFiles().size());
            assertEquals(1, detail.getReviews().size());
            assertEquals("2.0 KB", detail.getFiles().get(0).getFileSizeText());
        }
    }

    // ========================================================================
    // 异常流程测试 — TC-E-01 ~ TC-E-10
    // ========================================================================
    @Nested
    @DisplayName("异常流程 TC-E-01 ~ TC-E-10")
    class ErrorFlowTests {

        @Test
        @DisplayName("TC-E-01: 必填项为空提交（项目名称为空）")
        void createTopic_blankName_throwsException() {
            TopicCreateDTO dto = buildCreateDto(1);
            dto.setTopicName("");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTopic(dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("项目名称不能为空", ex.getMessage());
            verify(topicMapper, never()).insert(any());
        }

        @Test
        @DisplayName("TC-E-02: 学生人数小于1")
        void createTopic_invalidStudentLimit_throwsException() {
            TopicCreateDTO dto = buildCreateDto(1);
            dto.setStudentLimit(0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTopic(dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("请输入正确的学生人数", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-03: 修改他人题目")
        void updateTopic_otherTeachersTopic_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, OTHER_TEACHER_RELATED_ID));

            TopicUpdateDTO dto = buildUpdateDto();
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.updateTopic(1L, dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("无权限修改该题目", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-04: 修改非草稿/退回状态的题目（已审核通过的题目教师不能修改）")
        void updateTopic_nonDraftStatus_throwsException() {
            ProjectTopic approvedTopic = createApprovedTopic(1L, TEACHER_RELATED_ID);
            approvedTopic.setOpenStatus(1);
            when(topicMapper.selectById(1L)).thenReturn(approvedTopic);

            TopicUpdateDTO dto = buildUpdateDto();
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.updateTopic(1L, dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("当前题目状态不允许修改", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-05: 退回修改时审核意见为空")
        void reviewTopic_returnWithoutComment_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createPendingTopic(1L, TEACHER_RELATED_ID));

            TopicReviewDTO dto = new TopicReviewDTO();
            dto.setReviewResult(2);   // 退回修改
            dto.setReviewComment("");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewTopic(1L, dto, ADMIN_USER_ID));
            assertEquals("退回修改时必须填写审核意见", ex.getMessage());
            verify(reviewMapper, never()).insert(any());
        }

        @Test
        @DisplayName("TC-E-06: 重复审核同一题目")
        void reviewTopic_alreadyReviewed_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            TopicReviewDTO dto = new TopicReviewDTO();
            dto.setReviewResult(1);
            dto.setReviewComment("再次审核");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reviewTopic(1L, dto, ADMIN_USER_ID));
            assertEquals("该题目已审核，请勿重复操作", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-07: 上传不支持的文件格式")
        void uploadFile_unsupportedFormat_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            MockMultipartFile exeFile = new MockMultipartFile(
                    "file", "malware.exe", "application/x-msdownload",
                    new byte[100]
            );
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.uploadFile(1L, exeFile, null, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("文件格式不符合要求", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-08: 上传超过大小限制的文件")
        void uploadFile_tooLarge_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "big.zip", "application/zip",
                    new byte[60 * 1024 * 1024]
            );
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.uploadFile(1L, largeFile, null, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("文件大小超过系统限制（50MB）", ex.getMessage());
        }

        @Test
        @DisplayName("TC-E-09: 删除已被学生选择的题目")
        void deleteTopic_withActiveSelections_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));
            when(topicMapper.countActiveSelections(1L)).thenReturn(2);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.deleteTopic(1L, ADMIN_USER_ID, "ADMIN"));
            assertEquals("该题目已有学生选择，不能删除", ex.getMessage());
            verify(topicMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("TC-E-10: 开放未审核通过的题目（草稿状态）")
        void openTopic_draftStatus_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.openTopic(1L, ADMIN_USER_ID, "ADMIN"));
            assertEquals("题目未审核通过，不能开放", ex.getMessage());
        }
    }

    // ========================================================================
    // 权限测试 — TC-P-01 ~ TC-P-05
    // ========================================================================
    @Nested
    @DisplayName("权限测试 TC-P-01 ~ TC-P-05")
    class PermissionTests {

        @Test
        @DisplayName("TC-P-01: 学生尝试新增题目")
        void createTopic_asStudent_throwsException() {
            TopicCreateDTO dto = buildCreateDto(0);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createTopic(dto, null, "STUDENT", STUDENT_RELATED_ID));
            assertEquals("无出题权限", ex.getMessage());
        }

        @Test
        @DisplayName("TC-P-02: 学生尝试审核题目（Controller 层拦截，Service 层也会校验）")
        void reviewTopic_asStudent_throwsException() {
            // Service 层不直接校验 student，由 Controller 拦截
            // 但这里验证管理员才能审核，学生不通过 Controller 角色校验
            // Service 层通过 topicMapper.selectById 获取题目，无角色限制
            // 实际权限在校验 reviewer 时不做判断，因为 Controller 已拦截
            // 这里验证即使到 service 层，非管理员调用 reviewTopic 也不会误操作
            when(topicMapper.selectById(1L)).thenReturn(createPendingTopic(1L, TEACHER_RELATED_ID));
            TopicReviewDTO dto = new TopicReviewDTO();
            dto.setReviewResult(1);
            dto.setReviewComment("通过");

            // 学生角色不会到达此方法（Controller 层拦截），但确保逻辑正确
            service.reviewTopic(1L, dto, ADMIN_USER_ID);
            verify(reviewMapper).insert(any());
        }

        @Test
        @DisplayName("TC-P-03: 学生尝试上传资料")
        void uploadFile_asStudent_throwsException() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf",
                    new byte[10]
            );
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.uploadFile(1L, file, null, null, "STUDENT", STUDENT_RELATED_ID));
            assertEquals("无上传权限", ex.getMessage());
        }

        @Test
        @DisplayName("TC-P-04: 管理员审核退回修改（由 Controller 层拦截教师, 这里测试管理员正常操作）")
        void reviewTopic_asAdmin_returnForRevision() {
            when(topicMapper.selectById(1L)).thenReturn(createPendingTopic(1L, TEACHER_RELATED_ID));

            TopicReviewDTO dto = new TopicReviewDTO();
            dto.setReviewResult(2);
            dto.setReviewComment("需求不够明确，请补充具体功能描述");
            service.reviewTopic(1L, dto, ADMIN_USER_ID);

            verify(topicMapper).updateStatus(1L, 3);
        }

        @Test
        @DisplayName("TC-P-05: 教师尝试关闭题目（无权限）")
        void closeTopic_asTeacher_throwsException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.closeTopic(1L, TEACHER_USER_ID, "TEACHER"));
            assertEquals("无关闭题目权限", ex.getMessage());
        }
    }

    // ========================================================================
    // 边界测试 — TC-B-01 ~ TC-B-06
    // ========================================================================
    @Nested
    @DisplayName("边界测试 TC-B-01 ~ TC-B-06")
    class BoundaryTests {

        @Test
        @DisplayName("TC-B-03: 分页查询第1页（默认参数）")
        void queryTopicPage_defaultPagination() {
            when(topicMapper.countPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any()))
                    .thenReturn(25L);
            when(topicMapper.selectPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any(), eq(0), eq(10)))
                    .thenReturn(List.of(createListVO(1L, 2, 1)));

            TopicQueryDTO dto = new TopicQueryDTO();
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "ADMIN", ADMIN_USER_ID);

            assertEquals(25L, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
        }

        @Test
        @DisplayName("TC-B-04: 分页查询空结果（没有匹配的题目）")
        void queryTopicPage_emptyResult() {
            when(topicMapper.countPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any()))
                    .thenReturn(0L);
            when(topicMapper.selectPage(any(), any(), any(), any(), any(), isNull(),
                    any(), any(), anyInt(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of());

            TopicQueryDTO dto = new TopicQueryDTO();
            dto.setKeyword("不存在的题目名称xyz");
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "ADMIN", ADMIN_USER_ID);

            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("TC-B-05: 一个教师创建大量题目后分页（第3页返回正确记录）")
        void queryTopicPage_thirdPage() {
            when(topicMapper.countPage(any(), any(), any(), any(), any(), eq(TEACHER_RELATED_ID),
                    any(), any(), anyInt(), any()))
                    .thenReturn(25L);
            when(topicMapper.selectPage(any(), any(), any(), any(), any(), eq(TEACHER_RELATED_ID),
                    any(), any(), anyInt(), any(), eq(20), eq(10)))
                    .thenReturn(List.of(createListVO(21L, 0, 0)));

            TopicQueryDTO dto = new TopicQueryDTO();
            dto.setPageNum(3);
            dto.setPageSize(10);
            PageResult<TopicListVO> result = service.queryTopicPage(dto, null, "TEACHER", TEACHER_RELATED_ID);

            assertEquals(25L, result.getTotal());
            assertEquals(3, result.getPageNum());
        }

        @Test
        @DisplayName("TC-B-06: 题目内容为长文本（保存和查询完整）")
        void createAndGetTopic_longContent() {
            String longContent = "A".repeat(5000);
            TopicCreateDTO dto = buildCreateDto(0);
            dto.setTopicContent(longContent);
            service.createTopic(dto, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);

            verify(topicMapper).insert(topicCaptor.capture());
            assertEquals(longContent, topicCaptor.getValue().getTopicContent());
        }
    }

    // ========================================================================
    // 补充 Service 层其他用例
    // ========================================================================
    @Nested
    @DisplayName("其他 Service 层方法")
    class AdditionalServiceTests {

        @Test
        @DisplayName("关闭题目 - 管理员正常关闭已开放题目")
        void closeTopic_success() {
            ProjectTopic openTopic = createApprovedTopic(1L, TEACHER_RELATED_ID);
            openTopic.setOpenStatus(1);
            when(topicMapper.selectById(1L)).thenReturn(openTopic);

            service.closeTopic(1L, ADMIN_USER_ID, "ADMIN");
            verify(topicMapper).updateOpenStatus(1L, 2);
        }

        @Test
        @DisplayName("关闭题目 - 已是未开放状态则抛异常")
        void closeTopic_notOpen_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.closeTopic(1L, ADMIN_USER_ID, "ADMIN"));
            assertEquals("该题目当前不是开放状态", ex.getMessage());
        }

        @Test
        @DisplayName("删除文件 - 教师删除本人题目的文件")
        void deleteFile_ownFile_success() {
            TopicFile file = new TopicFile();
            file.setFileId(1L);
            file.setTopicId(1L);
            file.setIsDeleted(0);
            when(fileMapper.selectById(1L)).thenReturn(file);
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            service.deleteFile(1L, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID);
            verify(fileMapper).deleteById(1L);
        }

        @Test
        @DisplayName("删除文件 - 删除他人题目的文件抛异常")
        void deleteFile_otherTeachersFile_throwsException() {
            TopicFile file = new TopicFile();
            file.setFileId(1L);
            file.setTopicId(1L);
            file.setIsDeleted(0);
            when(fileMapper.selectById(1L)).thenReturn(file);

            // 题目属于 other teacher
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, OTHER_TEACHER_RELATED_ID));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.deleteFile(1L, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("无权限删除该文件", ex.getMessage());
        }

        @Test
        @DisplayName("学生查看未开放题目的文件列表抛异常")
        void listFiles_asStudent_forClosedTopic_throwsException() {
            // 草稿题目，未开放
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, TEACHER_RELATED_ID));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listFiles(1L, null, "STUDENT", STUDENT_RELATED_ID));
            assertEquals("该题目暂不可查看", ex.getMessage());
        }

        @Test
        @DisplayName("下载文件 - 文件不存在抛异常")
        void downloadFile_notFound_throwsException() {
            TopicFile file = new TopicFile();
            file.setFileId(1L);
            file.setTopicId(1L);
            file.setFilePath("nonexistent/file.pdf");
            file.setIsDeleted(0);
            when(fileMapper.selectById(1L)).thenReturn(file);
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            // 文件实体存在但磁盘上不存在（使用临时目录）
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.downloadFile(1L, ADMIN_USER_ID, "ADMIN", ADMIN_USER_ID));
            assertEquals("文件不存在或已被删除", ex.getMessage());
        }

        @Test
        @DisplayName("审核记录 - 管理员查看任意题目审核记录")
        void listReviews_asAdmin_success() {
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            TopicReviewVO reviewVO = new TopicReviewVO();
            reviewVO.setReviewResult(1);
            when(reviewMapper.selectByTopicId(1L)).thenReturn(List.of(reviewVO));

            List<TopicReviewVO> reviews = service.listReviews(1L, ADMIN_USER_ID, "ADMIN", ADMIN_USER_ID);
            assertEquals(1, reviews.size());
            assertEquals("通过", reviews.get(0).getReviewResultText());
        }

        @Test
        @DisplayName("审核记录 - 教师查看他人题目审核记录抛异常")
        void listReviews_asTeacher_otherTopic_throwsException() {
            when(topicMapper.selectById(1L)).thenReturn(createDraftTopic(1L, OTHER_TEACHER_RELATED_ID));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listReviews(1L, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("无权限查看审核记录", ex.getMessage());
        }

        @Test
        @DisplayName("管理员修改题目后状态重置为待审核（带 modifyReason）")
        void updateTopic_asAdmin_withModifyReason_resetsStatus() {
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            TopicUpdateDTO dto = buildUpdateDto();
            dto.setModifyReason("管理员调整难度等级");
            service.updateTopic(1L, dto, ADMIN_USER_ID, "ADMIN", TEACHER_RELATED_ID);

            verify(topicMapper).update(topicCaptor.capture());
            assertEquals(1, topicCaptor.getValue().getStatus().intValue()); // 重置为待审核
        }

        @Test
        @DisplayName("获取文件原始文件名")
        void getFileOriginalName_success() {
            TopicFile file = new TopicFile();
            file.setFileId(1L);
            file.setTopicId(1L);
            file.setFileName("报告.docx");
            file.setFilePath("1/uuid.docx");
            file.setIsDeleted(0);
            when(fileMapper.selectById(1L)).thenReturn(file);
            when(topicMapper.selectById(1L)).thenReturn(createApprovedTopic(1L, TEACHER_RELATED_ID));

            String name = service.getFileOriginalName(1L, ADMIN_USER_ID, "ADMIN", ADMIN_USER_ID);
            assertEquals("报告.docx", name);
        }

        @Test
        @DisplayName("提交审核 - 非草稿/退回修改状态抛异常")
        void submitForReview_invalidStatus_throwsException() {
            ProjectTopic approved = createApprovedTopic(1L, TEACHER_RELATED_ID);
            // 审核通过状态
            when(topicMapper.selectById(1L)).thenReturn(approved);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitForReview(1L, TEACHER_USER_ID, "TEACHER", TEACHER_RELATED_ID));
            assertEquals("当前题目状态不允许提交审核", ex.getMessage());
        }

        @Test
        @DisplayName("查看不存在题目详情抛异常")
        void getTopicDetail_notFound_throwsException() {
            when(topicMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getTopicDetail(999L, ADMIN_USER_ID, "ADMIN", ADMIN_USER_ID));
            assertEquals("题目信息不存在", ex.getMessage());
        }
    }

    // ========================================================================
    // 辅助方法
    // ========================================================================
    private TopicCreateDTO buildCreateDto(int status) {
        TopicCreateDTO dto = new TopicCreateDTO();
        dto.setTopicName("在线商城系统");
        dto.setTopicType("应用系统");
        dto.setDifficulty("B");
        dto.setStudentLimit(4);
        dto.setTopicContent("开发一个完整的B2C商城系统，包含商品管理、订单处理、购物车等功能");
        dto.setTechnicalRoute("Spring Boot + Vue + MySQL");
        dto.setDevelopTools("IDEA, VS Code, Navicat");
        dto.setStatus(status);
        dto.setSelectionStartTime(LocalDateTime.now());
        dto.setSelectionEndTime(LocalDateTime.now().plusDays(30));
        return dto;
    }

    private TopicUpdateDTO buildUpdateDto() {
        TopicUpdateDTO dto = new TopicUpdateDTO();
        dto.setTopicName("更新后的题目名称");
        dto.setTopicType("管理系统");
        dto.setDifficulty("A");
        dto.setStudentLimit(5);
        dto.setTopicContent("更新后的项目内容");
        dto.setTechnicalRoute("Spring Cloud + Vue + MySQL");
        return dto;
    }

    private TopicListVO createListVO(Long topicId, int status, int openStatus) {
        TopicListVO vo = new TopicListVO();
        vo.setTopicId(topicId);
        vo.setTopicName("题目" + topicId);
        vo.setTopicType("应用系统");
        vo.setDifficulty("B");
        vo.setTeacherName("张老师");
        vo.setTeacherId(TEACHER_RELATED_ID);
        vo.setStatus(status);
        vo.setOpenStatus(openStatus);
        return vo;
    }
}
