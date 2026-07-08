package com.training.system.score.vo;

import java.math.BigDecimal;

public class ProgressVO {
    private Long teamId;
    private String teamName;
    private Long topicId;
    private String topicName;
    private Integer totalStageCount;
    private Integer evaluatedStageCount;
    private BigDecimal totalStageWeight;
    private BigDecimal evaluatedStageWeight;
    private BigDecimal averageStageScore;
    private BigDecimal weightedStageScore;
    private BigDecimal processDocScore;
    private BigDecimal processSystemScore;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public Integer getTotalStageCount() { return totalStageCount; }
    public void setTotalStageCount(Integer totalStageCount) { this.totalStageCount = totalStageCount; }
    public Integer getEvaluatedStageCount() { return evaluatedStageCount; }
    public void setEvaluatedStageCount(Integer evaluatedStageCount) { this.evaluatedStageCount = evaluatedStageCount; }
    public BigDecimal getTotalStageWeight() { return totalStageWeight; }
    public void setTotalStageWeight(BigDecimal totalStageWeight) { this.totalStageWeight = totalStageWeight; }
    public BigDecimal getEvaluatedStageWeight() { return evaluatedStageWeight; }
    public void setEvaluatedStageWeight(BigDecimal evaluatedStageWeight) { this.evaluatedStageWeight = evaluatedStageWeight; }
    public BigDecimal getAverageStageScore() { return averageStageScore; }
    public void setAverageStageScore(BigDecimal averageStageScore) { this.averageStageScore = averageStageScore; }
    public BigDecimal getWeightedStageScore() { return weightedStageScore; }
    public void setWeightedStageScore(BigDecimal weightedStageScore) { this.weightedStageScore = weightedStageScore; }
    public BigDecimal getProcessDocScore() { return processDocScore; }
    public void setProcessDocScore(BigDecimal processDocScore) { this.processDocScore = processDocScore; }
    public BigDecimal getProcessSystemScore() { return processSystemScore; }
    public void setProcessSystemScore(BigDecimal processSystemScore) { this.processSystemScore = processSystemScore; }
}

