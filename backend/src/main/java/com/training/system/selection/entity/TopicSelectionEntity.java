package com.training.system.selection.entity;

import java.time.LocalDateTime;

public class TopicSelectionEntity {
    private Long id;
    private Long teamId;
    private Long topicId;
    private String selectionReason;
    private String status;
    private Long auditTeacherId;
    private String auditOpinion;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getSelectionReason() { return selectionReason; }
    public void setSelectionReason(String selectionReason) { this.selectionReason = selectionReason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAuditTeacherId() { return auditTeacherId; }
    public void setAuditTeacherId(Long auditTeacherId) { this.auditTeacherId = auditTeacherId; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
}
