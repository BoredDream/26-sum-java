package com.training.system.attendance.vo;

import java.time.LocalDateTime;

/**
 * 补签申请列表项
 */
public class MakeupApplyVO {

    private Long applyId;
    private Long taskId;
    private String taskTitle;
    private Long recordId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String applyReason;
    private String proofFilePath;
    private Integer auditStatus;
    private String auditStatusName;
    private Long auditTeacherId;
    private String auditTeacherName;
    private String auditComment;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getProofFilePath() {
        return proofFilePath;
    }

    public void setProofFilePath(String proofFilePath) {
        this.proofFilePath = proofFilePath;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditStatusName() {
        return auditStatusName;
    }

    public void setAuditStatusName(String auditStatusName) {
        this.auditStatusName = auditStatusName;
    }

    public Long getAuditTeacherId() {
        return auditTeacherId;
    }

    public void setAuditTeacherId(Long auditTeacherId) {
        this.auditTeacherId = auditTeacherId;
    }

    public String getAuditTeacherName() {
        return auditTeacherName;
    }

    public void setAuditTeacherName(String auditTeacherName) {
        this.auditTeacherName = auditTeacherName;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
