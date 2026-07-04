package com.training.system.score.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ContributionDTO {
    @NotNull
    private Long teamId;
    private Long stageId;
    @NotNull
    private Long studentId;
    @NotNull
    private Integer evaluationType;
    @NotNull
    private BigDecimal contributionScore;
    @NotNull
    private BigDecimal workloadRatio;
    @NotBlank
    private String workDescription;
    private String comment;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
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
}

