package com.training.system.score.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentScore {
    private Long studentScoreId;
    private Long scoreId;
    private Long teamId;
    private Long studentId;
    private BigDecimal contributionFactor;
    private BigDecimal personalScore;
    private String grade;
    private String teacherComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getStudentScoreId() { return studentScoreId; }
    public void setStudentScoreId(Long studentScoreId) { this.studentScoreId = studentScoreId; }
    public Long getScoreId() { return scoreId; }
    public void setScoreId(Long scoreId) { this.scoreId = scoreId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getContributionFactor() { return contributionFactor; }
    public void setContributionFactor(BigDecimal contributionFactor) { this.contributionFactor = contributionFactor; }
    public BigDecimal getPersonalScore() { return personalScore; }
    public void setPersonalScore(BigDecimal personalScore) { this.personalScore = personalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

