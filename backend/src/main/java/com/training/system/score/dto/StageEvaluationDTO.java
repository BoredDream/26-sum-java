package com.training.system.score.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class StageEvaluationDTO {
    @NotNull
    private Long stageId;
    @NotNull
    private Long teamId;
    private Long relatedDocumentId;
    @NotNull
    private BigDecimal docScore;
    @NotNull
    private BigDecimal completionScore;
    private BigDecimal innovationScore;
    private BigDecimal techScore;
    private String comment;
    @NotNull
    private Integer result;
    private Integer isLate = 0;
    private Integer lateDays = 0;

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
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public Integer getIsLate() { return isLate; }
    public void setIsLate(Integer isLate) { this.isLate = isLate; }
    public Integer getLateDays() { return lateDays; }
    public void setLateDays(Integer lateDays) { this.lateDays = lateDays; }
}

