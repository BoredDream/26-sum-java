package com.training.system.score.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StageTaskVO {
    private Long stageId;
    private String stageName;
    private String stageDesc;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String deliverables;
    private String scoringCriteria;
    private BigDecimal weight;
    private Long teacherId;
    private String teacherName;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getStageDesc() { return stageDesc; }
    public void setStageDesc(String stageDesc) { this.stageDesc = stageDesc; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    public String getScoringCriteria() { return scoringCriteria; }
    public void setScoringCriteria(String scoringCriteria) { this.scoringCriteria = scoringCriteria; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

