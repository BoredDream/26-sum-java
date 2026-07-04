package com.training.system.score.vo;

import java.math.BigDecimal;

public class StudentScoreVO {
    private Long studentScoreId;
    private Long scoreId;
    private Long teamId;
    private String teamName;
    private Long studentId;
    private String studentName;
    private BigDecimal contributionFactor;
    private BigDecimal personalScore;
    private String grade;
    private String teacherComment;

    public Long getStudentScoreId() { return studentScoreId; }
    public void setStudentScoreId(Long studentScoreId) { this.studentScoreId = studentScoreId; }
    public Long getScoreId() { return scoreId; }
    public void setScoreId(Long scoreId) { this.scoreId = scoreId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public BigDecimal getContributionFactor() { return contributionFactor; }
    public void setContributionFactor(BigDecimal contributionFactor) { this.contributionFactor = contributionFactor; }
    public BigDecimal getPersonalScore() { return personalScore; }
    public void setPersonalScore(BigDecimal personalScore) { this.personalScore = personalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
}

