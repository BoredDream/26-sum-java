package com.training.system.score.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StageEvaluation {
    private Long evaluationId;
    private Long stageId;
    private Long teamId;
    private Long relatedDocumentId;
    private BigDecimal docScore;
    private BigDecimal completionScore;
    private BigDecimal innovationScore;
    private BigDecimal techScore;
    private BigDecimal totalScore;
    private String comment;
    private Integer result;
    private Long teacherId;
    private Integer isLate;
    private Integer lateDays;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getRelatedDocumentId() { return relatedDocumentId; }
    public void setRelatedDocumentId(Long relatedDocumentId) { this.relatedDocumentId = relatedDocumentId; }
    public BigDecimal getDocScore() { return docScore; }
    public void setDocScore(BigDecimal docScore) { this.docScore = docScore; }
    public BigDecimal getCompletionScore() { return completionScore; }
    public void setCompletionScore(BigDecimal completionScore) { this.completionScore = completionScore; }
    public BigDecimal getInnovationScore() { return innovationScore; }
    public void setInnovationScore(BigDecimal innovationScore) { this.innovationScore = innovationScore; }
    public BigDecimal getTechScore() { return techScore; }
    public void setTechScore(BigDecimal techScore) { this.techScore = techScore; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Integer getIsLate() { return isLate; }
    public void setIsLate(Integer isLate) { this.isLate = isLate; }
    public Integer getLateDays() { return lateDays; }
    public void setLateDays(Integer lateDays) { this.lateDays = lateDays; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

