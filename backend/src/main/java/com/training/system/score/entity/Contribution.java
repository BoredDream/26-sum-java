package com.training.system.score.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Contribution {
    private Long contributionId;
    private Long teamId;
    private Long stageId;
    private Long studentId;
    private Long evaluatorId;
    private Integer evaluationType;
    private BigDecimal contributionScore;
    private BigDecimal workloadRatio;
    private String workDescription;
    private String comment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getContributionId() { return contributionId; }
    public void setContributionId(Long contributionId) { this.contributionId = contributionId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }
    public Integer getEvaluationType() { return evaluationType; }
    public void setEvaluationType(Integer evaluationType) { this.evaluationType = evaluationType; }
    public BigDecimal getContributionScore() { return contributionScore; }
    public void setContributionScore(BigDecimal contributionScore) { this.contributionScore = contributionScore; }
    public BigDecimal getWorkloadRatio() { return workloadRatio; }
    public void setWorkloadRatio(BigDecimal workloadRatio) { this.workloadRatio = workloadRatio; }
    public String getWorkDescription() { return workDescription; }
    public void setWorkDescription(String workDescription) { this.workDescription = workDescription; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

