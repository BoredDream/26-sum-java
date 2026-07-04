package com.training.system.score.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class StudentScoreSaveDTO {
    @NotNull
    private Long studentId;
    private BigDecimal contributionFactor = BigDecimal.ONE;
    private String teacherComment;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getContributionFactor() { return contributionFactor; }
    public void setContributionFactor(BigDecimal contributionFactor) { this.contributionFactor = contributionFactor; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
}

