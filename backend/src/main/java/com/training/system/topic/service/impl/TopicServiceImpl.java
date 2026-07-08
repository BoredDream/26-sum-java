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
import com.training.system.topic.service.TopicService;
import com.training.system.topic.vo.TopicDetailVO;
import com.training.system.topic.vo.TopicFileVO;
import com.training.system.topic.vo.TopicListVO;
import com.training.system.topic.vo.TopicReviewVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 出题管理服务实现
 */
@Service
public class TopicServiceImpl implements TopicService {

    private final ProjectTopicMapper topicMapper;
    private final TopicFileMapper fileMapper;
    private final TopicReviewMapper reviewMapper;

    @Value("${topic.file.upload-dir:./uploads/topic}")
    private String uploadDir;

    /** 允许上传的文件扩展名 */
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx",
            "zip", "rar", "txt", "png", "jpg", "jpeg"
    );

    /** 文件大小上限：50MB */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /** 审核状态文本映射 */
    private static final Map<Integer, String> STATUS_TEXT = Map.of(
            0, "草稿",
            1, "待审核",
            2, "审核通过",
            3, "退回修改",
            4, "不予通过"
    );

    /** 开放状态文本映射 */
    private static final Map<Integer, String> OPEN_STATUS_TEXT = Map.of(
            0, "未开放",
            1, "已开放",
            2, "已关闭"
    );

    /** 审核结果文本映射 */
    private static final Map<Integer, String> REVIEW_RESULT_TEXT = Map.of(
            1, "通过",
            2, "退回修改",
            3, "不予通过"
    );

    public TopicServiceImpl(ProjectTopicMapper topicMapper,
                            TopicFileMapper fileMapper,
                            TopicReviewMapper reviewMapper) {
        this.topicMapper = topicMapper;
        this.fileMapper = fileMapper;
        this.reviewMapper = reviewMapper;
    }

    // ==================== 新增题目 ====================

    @Override
    public Long createTopic(TopicCreateDTO dto, Long userId, String role, Long relatedId) {
        // 权限校验
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无出题权限");
        }

        // 如果提交审核，校验必填项
        if (dto.getStatus() == 1) {
            validateRequiredFields(dto);
        }

        ProjectTopic topic = new ProjectTopic();
        topic.setTopicName(dto.getTopicName());
        topic.setTopicType(dto.getTopicType());
        topic.setDifficulty(dto.getDifficulty());
        topic.setTeacherId(relatedId);  // 教师的 teacher_id
        topic.setStudentLimit(dto.getStudentLimit());
        topic.setTeamLimit(dto.getTeamLimit());
        topic.setTopicContent(dto.getTopicContent());
        topic.setDevelopTools(dto.getDevelopTools());
        topic.setTechnicalRoute(dto.getTechnicalRoute());
        topic.setSelectionStartTime(dto.getSelectionStartTime());
        topic.setSelectionEndTime(dto.getSelectionEndTime());
        topic.setStatus(dto.getStatus());         // 0-草稿, 1-提交审核
        topic.setOpenStatus(0);                   // 默认未开放

        topicMapper.insert(topic);
        return topic.getTopicId();
    }

    // ==================== 修改题目 ====================

    @Override
    public void updateTopic(Long topicId, TopicUpdateDTO dto, Long userId, String role, Long relatedId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 权限校验：教师只能修改本人题目
        if ("TEACHER".equals(role)) {
            if (!topic.getTeacherId().equals(relatedId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权限修改该题目");
            }
            // 教师只能在草稿、待审核或退回修改状态下修改
            if (topic.getStatus() != 0 && topic.getStatus() != 1 && topic.getStatus() != 3) {
                throw new BusinessException(ResultCode.CONFLICT, "当前题目状态不允许修改");
            }
        } else if (!"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限修改该题目");
        }

        // 已开放题目不能修改核心信息
        if (topic.getOpenStatus() == 1 && "TEACHER".equals(role)) {
            throw new BusinessException(ResultCode.CONFLICT, "题目已开放，不能随意修改核心信息");
        }

        topic.setTopicName(dto.getTopicName());
        topic.setTopicType(dto.getTopicType());
        topic.setDifficulty(dto.getDifficulty());
        topic.setStudentLimit(dto.getStudentLimit());
        topic.setTeamLimit(dto.getTeamLimit());
        topic.setTopicContent(dto.getTopicContent());
        topic.setDevelopTools(dto.getDevelopTools());
        topic.setTechnicalRoute(dto.getTechnicalRoute());
        topic.setSelectionStartTime(dto.getSelectionStartTime());
        topic.setSelectionEndTime(dto.getSelectionEndTime());

        // 管理员修改核心字段后，题目需要重新审核
        if ("ADMIN".equals(role) && dto.getModifyReason() != null && !dto.getModifyReason().isEmpty()) {
            topic.setStatus(1); // 重置为待审核
        }

        topicMapper.update(topic);
    }

    // ==================== 删除题目 ====================

    @Override
    public void deleteTopic(Long topicId, Long userId, String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限删除题目");
        }

        ProjectTopic topic = getTopicOrThrow(topicId);

        // 检查是否已有学生选择
        int selectionCount = topicMapper.countActiveSelections(topicId);
        if (selectionCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该题目已有学生选择，不能删除");
        }

        topicMapper.deleteById(topicId);
    }

    // ==================== 查看题目详情 ====================

    @Override
    public TopicDetailVO getTopicDetail(Long topicId, Long userId, String role, Long relatedId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 学生只能查看已审核通过且已开放的题目
        if ("STUDENT".equals(role)) {
            if (topic.getStatus() != 2 || topic.getOpenStatus() != 1) {
                throw new BusinessException(ResultCode.FORBIDDEN, "该题目暂不可查看");
            }
        }

        // 教师只能查看自己创建的题目（管理员看全部）
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看该题目");
        }

        return buildDetailVO(topic);
    }

    // ==================== 分页查询 ====================

    @Override
    public PageResult<TopicListVO> queryTopicPage(TopicQueryDTO dto, Long userId, String role, Long relatedId) {
        List<Integer> statusList = null;
        Long filterTeacherId = dto.getTeacherId();
        Integer filterStatus = dto.getStatus();
        Integer filterOpenStatus = dto.getOpenStatus();

        // 根据角色设置可见范围
        switch (role) {
            case "STUDENT":
                // 学生只能看到已审核通过且已开放的题目
                filterStatus = 2;
                filterOpenStatus = 1;
                break;
            case "TEACHER":
                // 教师默认只看自己的题目
                if (filterTeacherId == null) {
                    filterTeacherId = relatedId;
                }
                break;
            case "ADMIN":
                // 管理员看全部，不限制
                break;
            default:
                throw new BusinessException(ResultCode.FORBIDDEN, "无效的用户角色");
        }

        int offset = (dto.getPageNum() - 1) * dto.getPageSize();

        List<TopicListVO> records = topicMapper.selectPage(
                dto.getKeyword(), dto.getTopicType(), dto.getDifficulty(),
                filterStatus, filterOpenStatus, filterTeacherId,
                dto.getStartTime(), dto.getEndTime(),
                0, statusList, offset, dto.getPageSize()
        );

        long total = topicMapper.countPage(
                dto.getKeyword(), dto.getTopicType(), dto.getDifficulty(),
                filterStatus, filterOpenStatus, filterTeacherId,
                dto.getStartTime(), dto.getEndTime(),
                0, statusList
        );

        // 填充状态文本
        for (TopicListVO vo : records) {
            vo.setStatusText(STATUS_TEXT.getOrDefault(vo.getStatus(), "未知"));
            vo.setOpenStatusText(OPEN_STATUS_TEXT.getOrDefault(vo.getOpenStatus(), "未知"));
        }

        return new PageResult<>(records, total, dto.getPageNum(), dto.getPageSize());
    }

    // ==================== 提交审核 ====================

    @Override
    @Transactional
    public void submitForReview(Long topicId, Long userId, String role, Long relatedId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 权限校验
        if (!"ADMIN".equals(role)) {
            if (!"TEACHER".equals(role) || !topic.getTeacherId().equals(relatedId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该题目");
            }
        }

        // 状态校验
        if (topic.getStatus() != 0 && topic.getStatus() != 3) {
            throw new BusinessException(ResultCode.CONFLICT, "当前题目状态不允许提交审核");
        }

        // 必填项校验
        if (isBlank(topic.getTopicName()) || isBlank(topic.getTopicType())
                || isBlank(topic.getDifficulty()) || topic.getStudentLimit() == null
                || topic.getStudentLimit() < 1 || isBlank(topic.getTopicContent())
                || isBlank(topic.getTechnicalRoute())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "题目信息不完整，请补全必填项后提交");
        }

        topicMapper.updateStatus(topicId, 1);
    }

    // ==================== 审核题目 ====================

    @Override
    @Transactional
    public void reviewTopic(Long topicId, TopicReviewDTO dto, Long userId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 只有待审核状态可以审核
        if (topic.getStatus() != 1) {
            throw new BusinessException(ResultCode.CONFLICT, "该题目已审核，请勿重复操作");
        }

        // 退回修改时必须填写审核意见
        if (dto.getReviewResult() == 2 && isBlank(dto.getReviewComment())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退回修改时必须填写审核意见");
        }

        // 新增审核记录
        TopicReview review = new TopicReview();
        review.setTopicId(topicId);
        review.setAdminId(userId);
        review.setReviewResult(dto.getReviewResult());
        review.setReviewComment(dto.getReviewComment());
        reviewMapper.insert(review);

        // 更新题目状态
        int newStatus;
        switch (dto.getReviewResult()) {
            case 1 -> newStatus = 2; // 通过
            case 2 -> newStatus = 3; // 退回修改
            case 3 -> newStatus = 4; // 不予通过
            default -> throw new BusinessException(ResultCode.BAD_REQUEST, "无效的审核结果");
        }
        topicMapper.updateStatus(topicId, newStatus);
    }

    // ==================== 开放题目 ====================

    @Override
    public void openTopic(Long topicId, Long userId, String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无开放题目权限");
        }

        ProjectTopic topic = getTopicOrThrow(topicId);

        if (topic.getStatus() != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "题目未审核通过，不能开放");
        }

        if (topic.getOpenStatus() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "该题目已开放");
        }

        topicMapper.updateOpenStatus(topicId, 1);
    }

    // ==================== 关闭题目 ====================

    @Override
    public void closeTopic(Long topicId, Long userId, String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无关闭题目权限");
        }

        ProjectTopic topic = getTopicOrThrow(topicId);

        if (topic.getOpenStatus() != 1) {
            throw new BusinessException(ResultCode.CONFLICT, "该题目当前不是开放状态");
        }

        topicMapper.updateOpenStatus(topicId, 2);
    }

    // ==================== 上传资料 ====================

    @Override
    public void uploadFile(Long topicId, MultipartFile file, String fileDesc,
                           Long userId, String role, Long relatedId) {
        // 权限校验
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无上传权限");
        }

        ProjectTopic topic = getTopicOrThrow(topicId);

        // 教师只能为本人题目上传资料
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限为该题目上传资料");
        }

        // 校验文件
        validateFile(file);

        // 存储文件
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        String relativePath = topicId + "/" + storedFilename;

        try {
            Path uploadPath = Paths.get(uploadDir, String.valueOf(topicId));
            Files.createDirectories(uploadPath);
            file.transferTo(uploadPath.resolve(storedFilename));
        } catch (IOException e) {
            throw new BusinessException(ResultCode.ERROR, "文件上传失败，请重试");
        }

        // 保存文件记录
        TopicFile topicFile = new TopicFile();
        topicFile.setTopicId(topicId);
        topicFile.setFileName(originalFilename);
        topicFile.setFilePath(relativePath);
        topicFile.setFileType(extension);
        topicFile.setFileSize(file.getSize());
        topicFile.setUploadUserId(userId);
        topicFile.setFileDesc(fileDesc);

        fileMapper.insert(topicFile);
    }

    // ==================== 删除资料 ====================

    @Override
    public void deleteFile(Long fileId, Long userId, String role, Long relatedId) {
        TopicFile file = fileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        // 权限：教师（本人题目）或管理员
        ProjectTopic topic = getTopicOrThrow(file.getTopicId());
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限删除该文件");
        }
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限删除该文件");
        }

        fileMapper.deleteById(fileId);
    }

    // ==================== 查看资料列表 ====================

    @Override
    public List<TopicFileVO> listFiles(Long topicId, Long userId, String role, Long relatedId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 学生只能查看已开放题目的资料
        if ("STUDENT".equals(role) && (topic.getStatus() != 2 || topic.getOpenStatus() != 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该题目暂不可查看");
        }

        // 教师只能查看自己题目的资料
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看该题目资料");
        }

        List<TopicFileVO> files = fileMapper.selectByTopicId(topicId);

        // 格式化文件大小
        for (TopicFileVO vo : files) {
            vo.setFileSizeText(formatFileSize(vo.getFileSize()));
        }

        return files;
    }

    // ==================== 下载资料 ====================

    @Override
    public Resource downloadFile(Long fileId, Long userId, String role, Long relatedId) {
        TopicFile file = validateFileAccess(fileId, userId, role, relatedId);

        Path filePath = Paths.get(uploadDir, file.getFilePath()).normalize();
        File diskFile = filePath.toFile();

        if (!diskFile.exists()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已被删除");
        }

        return new FileSystemResource(diskFile);
    }

    @Override
    public String getFileOriginalName(Long fileId, Long userId, String role, Long relatedId) {
        TopicFile file = validateFileAccess(fileId, userId, role, relatedId);
        return file.getFileName();
    }

    /**
     * 校验文件访问权限，返回文件实体
     */
    private TopicFile validateFileAccess(Long fileId, Long userId, String role, Long relatedId) {
        TopicFile file = fileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        ProjectTopic topic = getTopicOrThrow(file.getTopicId());

        if ("STUDENT".equals(role) && (topic.getStatus() != 2 || topic.getOpenStatus() != 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限下载该文件");
        }
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限下载该文件");
        }

        return file;
    }

    // ==================== 查看审核记录 ====================

    @Override
    public List<TopicReviewVO> listReviews(Long topicId, Long userId, String role, Long relatedId) {
        ProjectTopic topic = getTopicOrThrow(topicId);

        // 教师只能查看自己题目的审核记录
        if ("TEACHER".equals(role) && !topic.getTeacherId().equals(relatedId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看审核记录");
        }

        List<TopicReviewVO> reviews = reviewMapper.selectByTopicId(topicId);
        for (TopicReviewVO vo : reviews) {
            vo.setReviewResultText(REVIEW_RESULT_TEXT.getOrDefault(vo.getReviewResult(), "未知"));
        }

        return reviews;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取题目，不存在或已删除则抛异常
     */
    private ProjectTopic getTopicOrThrow(Long topicId) {
        ProjectTopic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目信息不存在");
        }
        return topic;
    }

    /**
     * 构建题目详情VO
     */
    private TopicDetailVO buildDetailVO(ProjectTopic topic) {
        TopicDetailVO vo = new TopicDetailVO();
        vo.setTopicId(topic.getTopicId());
        vo.setTopicName(topic.getTopicName());
        vo.setTopicType(topic.getTopicType());
        vo.setDifficulty(topic.getDifficulty());
        vo.setTeacherId(topic.getTeacherId());
        vo.setStudentLimit(topic.getStudentLimit());
        vo.setTeamLimit(topic.getTeamLimit());
        vo.setSelectionStartTime(topic.getSelectionStartTime());
        vo.setSelectionEndTime(topic.getSelectionEndTime());
        vo.setTopicContent(topic.getTopicContent());
        vo.setDevelopTools(topic.getDevelopTools());
        vo.setTechnicalRoute(topic.getTechnicalRoute());
        vo.setStatus(topic.getStatus());
        vo.setStatusText(STATUS_TEXT.getOrDefault(topic.getStatus(), "未知"));
        vo.setOpenStatus(topic.getOpenStatus());
        vo.setOpenStatusText(OPEN_STATUS_TEXT.getOrDefault(topic.getOpenStatus(), "未知"));
        vo.setCreateTime(topic.getCreateTime());
        vo.setUpdateTime(topic.getUpdateTime());

        // 填充文件列表
        List<TopicFileVO> files = fileMapper.selectByTopicId(topic.getTopicId());
        for (TopicFileVO f : files) {
            f.setFileSizeText(formatFileSize(f.getFileSize()));
        }
        vo.setFiles(files);

        // 填充审核记录
        List<TopicReviewVO> reviews = reviewMapper.selectByTopicId(topic.getTopicId());
        for (TopicReviewVO r : reviews) {
            r.setReviewResultText(REVIEW_RESULT_TEXT.getOrDefault(r.getReviewResult(), "未知"));
        }
        vo.setReviews(reviews);

        return vo;
    }

    /**
     * 校验必填字段
     */
    private void validateRequiredFields(TopicCreateDTO dto) {
        if (isBlank(dto.getTopicName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "项目名称不能为空");
        }
        if (isBlank(dto.getTopicType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "项目类型不能为空");
        }
        if (isBlank(dto.getDifficulty())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "项目难度不能为空");
        }
        if (dto.getStudentLimit() == null || dto.getStudentLimit() < 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请输入正确的学生人数");
        }
        if (isBlank(dto.getTopicContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "项目内容及要求不能为空");
        }
        if (isBlank(dto.getTechnicalRoute())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "技术路线不能为空");
        }
    }

    /**
     * 校验上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择上传文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小超过系统限制（50MB）");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件格式不符合要求");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 字符串判空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
