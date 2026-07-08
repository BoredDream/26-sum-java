package com.training.system.score.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScoreVO {
    private Long scoreId;
    private Long teamId;
    private String teamName;
    private Long topicId;
    private String topicName;
    private Long teacherId;
    private String teacherName;
    private BigDecimal docScore;
    private BigDecimal attendanceScore;
    private BigDecimal systemScore;
    private BigDecimal defenseScore;
    private BigDecimal totalScore;
    private Integer totalStageCount;
    private Integer evaluatedStageCount;
    private BigDecimal totalStageWeight;
    private BigDecimal evaluatedStageWeight;
    private BigDecimal processScore;
    private BigDecimal processDocScore;
    private BigDecimal processSystemScore;
    private BigDecimal suggestedDocScore;
    private BigDecimal suggestedSystemScore;
    private String teacherComment;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getScoreId() { return scoreId; }
    public void setScoreId(Long scoreId) { this.scoreId = scoreId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public BigDecimal getDocScore() { return docScore; }
    public void setDocScore(BigDecimal docScore) { this.docScore = docScore; }
    public BigDecimal getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }
    public BigDecimal getSystemScore() { return systemScore; }
    public void setSystemScore(BigDecimal systemScore) { this.systemScore = systemScore; }
    public BigDecimal getDefenseScore() { return defenseScore; }
    public void setDefenseScore(BigDecimal defenseScore) { this.defenseScore = defenseScore; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public Integer getTotalStageCount() { return totalStageCount; }
    public void setTotalStageCount(Integer totalStageCount) { this.totalStageCount = totalStageCount; }
    public Integer getEvaluatedStageCount() { return evaluatedStageCount; }
    public void setEvaluatedStageCount(Integer evaluatedStageCount) { this.evaluatedStageCount = evaluatedStageCount; }
    public BigDecimal getTotalStageWeight() { return totalStageWeight; }
    public void setTotalStageWeight(BigDecimal totalStageWeight) { this.totalStageWeight = totalStageWeight; }
    public BigDecimal getEvaluatedStageWeight() { return evaluatedStageWeight; }
    public void setEvaluatedStageWeight(BigDecimal evaluatedStageWeight) { this.evaluatedStageWeight = evaluatedStageWeight; }
    public BigDecimal getProcessScore() { return processScore; }
    public void setProcessScore(BigDecimal processScore) { this.processScore = processScore; }
    public BigDecimal getProcessDocScore() { return processDocScore; }
    public void setProcessDocScore(BigDecimal processDocScore) { this.processDocScore = processDocScore; }
    public BigDecimal getProcessSystemScore() { return processSystemScore; }
    public void setProcessSystemScore(BigDecimal processSystemScore) { this.processSystemScore = processSystemScore; }
    public BigDecimal getSuggestedDocScore() { return suggestedDocScore; }
    public void setSuggestedDocScore(BigDecimal suggestedDocScore) { this.suggestedDocScore = suggestedDocScore; }
    public BigDecimal getSuggestedSystemScore() { return suggestedSystemScore; }
    public void setSuggestedSystemScore(BigDecimal suggestedSystemScore) { this.suggestedSystemScore = suggestedSystemScore; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

