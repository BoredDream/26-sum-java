package com.training.system.selection.entity;

import java.time.LocalDateTime;

public class ProcessDocumentEntity {
    private Long id;
    private Long teamId;
    private Long topicId;
    private String documentName;
    private String documentType;
    private String projectStage;
    private String versionNo;
    private String originalFilename;
    private String storedPath;
    private Long fileSize;
    private Long uploaderId;
    private String status;
    private String teacherFeedback;
    private Long feedbackTeacherId;
    private Long stageId;
    private String remark;
    private LocalDateTime uploadTime;
    private LocalDateTime feedbackTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getProjectStage() { return projectStage; }
    public void setProjectStage(String projectStage) { this.projectStage = projectStage; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }
    public Long getFeedbackTeacherId() { return feedbackTeacherId; }
    public void setFeedbackTeacherId(Long feedbackTeacherId) { this.feedbackTeacherId = feedbackTeacherId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    public LocalDateTime getFeedbackTime() { return feedbackTime; }
    public void setFeedbackTime(LocalDateTime feedbackTime) { this.feedbackTime = feedbackTime; }
}
