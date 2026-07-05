package com.training.system.selection.service;

import com.training.system.exception.BusinessException;
import com.training.system.common.ResultCode;

import com.training.system.selection.dto.DocumentFeedbackDTO;
import com.training.system.selection.entity.ProcessDocumentEntity;
import com.training.system.selection.entity.TeamEntity;
import com.training.system.selection.entity.TopicEntity;
import com.training.system.selection.mapper.ProcessDocumentMapper;
import com.training.system.selection.mapper.TopicMapper;
import com.training.system.selection.vo.ProcessDocumentVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.training.system.selection.service.SelectionConstants.*;

@Service
public class DocumentService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("doc", "docx", "pdf", "xls", "xlsx", "zip", "rar");

    private final TeamService teamService;
    private final TopicMapper topicMapper;
    private final ProcessDocumentMapper documentMapper;
    private final Path uploadRoot;

    public DocumentService(TeamService teamService,
                           TopicMapper topicMapper,
                           ProcessDocumentMapper documentMapper,
                           @Value("${selection.upload-dir:uploads/selection}") String uploadDir) {
        this.teamService = teamService;
        this.topicMapper = topicMapper;
        this.documentMapper = documentMapper;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建文件上传目录", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ProcessDocumentVO upload(Long userId, String role, String documentName, String documentType,
                                    String projectStage, MultipartFile file) {
        teamService.requireStudent(role);
        if (isBlank(documentName) || isBlank(documentType) || isBlank(projectStage)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文档名称、文档类型和所属阶段不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择需要上传的文件");
        }

        TeamEntity team = teamService.getCurrentTeamByStudent(userId);
        if (!TEAM_SELECTED.equals(team.getStatus()) || team.getSelectedTopicId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请在选题审核通过后再提交过程文档");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (originalFilename.isBlank() || originalFilename.contains("..")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件名不合法");
        }
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 doc、docx、pdf、xls、xlsx、zip、rar 格式文件");
        }
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.copy(file.getInputStream(), uploadRoot.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.ERROR, "文件保存失败，请稍后重试");
        }

        int version = documentMapper.countSameTypeAndStage(team.getId(), documentType.trim(), projectStage.trim()) + 1;
        ProcessDocumentEntity document = new ProcessDocumentEntity();
        document.setTeamId(team.getId());
        document.setTopicId(team.getSelectedTopicId());
        document.setDocumentName(documentName.trim());
        document.setDocumentType(documentType.trim());
        document.setProjectStage(projectStage.trim());
        document.setVersionNo("V" + version + ".0");
        document.setOriginalFilename(originalFilename);
        document.setStoredPath(storedFilename);
        document.setFileSize(file.getSize());
        document.setUploaderId(userId);
        document.setStatus(DOCUMENT_SUBMITTED);
        document.setUploadTime(LocalDateTime.now());
        documentMapper.insert(document);
        return toVO(document);
    }

    public List<ProcessDocumentVO> listMyScope(Long userId, String role) {
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            TeamEntity team = teamService.getCurrentTeamByStudent(userId);
            return documentMapper.findByTeamId(team.getId()).stream().map(this::toVO).toList();
        }
        if (ROLE_TEACHER.equalsIgnoreCase(role)) {
            return documentMapper.findByTeacherId(userId).stream().map(this::toVO).toList();
        }
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return documentMapper.findAll().stream().map(this::toVO).toList();
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无查看权限");
    }

    @Transactional(rollbackFor = Exception.class)
    public ProcessDocumentVO feedback(Long userId, String role, Long documentId, DocumentFeedbackDTO dto) {
        teamService.requireTeacherOrAdmin(role);
        ProcessDocumentEntity document = getDocumentById(documentId);
        assertTeacherCanAccess(userId, role, document.getTopicId());
        document.setTeacherFeedback(dto.getFeedback().trim());
        document.setFeedbackTeacherId(userId);
        document.setFeedbackTime(LocalDateTime.now());
        document.setStatus(Boolean.TRUE.equals(dto.getReturned()) ? DOCUMENT_RETURNED : DOCUMENT_REVIEWED);
        documentMapper.updateFeedback(document);
        return toVO(document);
    }

    public Resource download(Long userId, String role, Long documentId) {
        ProcessDocumentEntity document = getDocumentById(documentId);
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            if (!teamService.isTeamMember(document.getTeamId(), userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "只能下载本团队的过程文档");
            }
        } else {
            assertTeacherCanAccess(userId, role, document.getTopicId());
        }
        Path file = uploadRoot.resolve(document.getStoredPath()).normalize();
        if (!file.startsWith(uploadRoot) || !Files.exists(file)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "原始文件不存在");
        }
        return new FileSystemResource(file);
    }

    private ProcessDocumentEntity getDocumentById(Long id) {
        ProcessDocumentEntity document = documentMapper.findById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "过程文档不存在");
        }
        return document;
    }

    private void assertTeacherCanAccess(Long userId, String role, Long topicId) {
        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return;
        }
        teamService.requireTeacherOrAdmin(role);
        TopicEntity topic = topicMapper.findById(topicId);
        if (topic == null || !userId.equals(topic.getTeacherId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看或反馈该课题的过程文档");
        }
    }

    private ProcessDocumentVO toVO(ProcessDocumentEntity entity) {
        ProcessDocumentVO vo = new ProcessDocumentVO();
        vo.setId(entity.getId());
        vo.setTeamId(entity.getTeamId());
        vo.setTopicId(entity.getTopicId());
        vo.setDocumentName(entity.getDocumentName());
        vo.setDocumentType(entity.getDocumentType());
        vo.setProjectStage(entity.getProjectStage());
        vo.setVersionNo(entity.getVersionNo());
        vo.setOriginalFilename(entity.getOriginalFilename());
        vo.setUploaderId(entity.getUploaderId());
        vo.setStatus(entity.getStatus());
        vo.setTeacherFeedback(entity.getTeacherFeedback());
        vo.setFeedbackTeacherId(entity.getFeedbackTeacherId());
        vo.setUploadTime(entity.getUploadTime());
        vo.setFeedbackTime(entity.getFeedbackTime());
        return vo;
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
