package com.training.system.score.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ScoreSaveDTO {
    @NotNull
    private Long teamId;
    private Long aiReportId;
    @NotNull
    private BigDecimal docScore;
    @NotNull
    private BigDecimal attendanceScore;
    @NotNull
    private BigDecimal systemScore;
    @NotNull
    private BigDecimal defenseScore;
    private String teacherComment;
    private List<StudentScoreSaveDTO> studentScores;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getAiReportId() { return aiReportId; }
    public void setAiReportId(Long aiReportId) { this.aiReportId = aiReportId; }
    public BigDecimal getDocScore() { return docScore; }
    public void setDocScore(BigDecimal docScore) { this.docScore = docScore; }
    public BigDecimal getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }
    public BigDecimal getSystemScore() { return systemScore; }
    public void setSystemScore(BigDecimal systemScore) { this.systemScore = systemScore; }
    public BigDecimal getDefenseScore() { return defenseScore; }
    public void setDefenseScore(BigDecimal defenseScore) { this.defenseScore = defenseScore; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public List<StudentScoreSaveDTO> getStudentScores() { return studentScores; }
    public void setStudentScores(List<StudentScoreSaveDTO> studentScores) { this.studentScores = studentScores; }
}

