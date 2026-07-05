package com.training.system.score.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScoreVO {
    private Long scoreId;
    private Long teamId;
    private String teamName;
    private Long teacherId;
    private String teacherName;
    private BigDecimal docScore;
    private BigDecimal attendanceScore;
    private BigDecimal systemScore;
    private BigDecimal defenseScore;
    private BigDecimal totalScore;
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

