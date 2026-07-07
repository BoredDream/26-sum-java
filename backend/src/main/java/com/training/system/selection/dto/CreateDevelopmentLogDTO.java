package com.training.system.selection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateDevelopmentLogDTO {
    @NotBlank(message = "日志标题不能为空")
    @Size(max = 200, message = "日志标题不能超过200个字符")
    private String title;

    @NotNull(message = "日志日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;

    @NotBlank(message = "工作内容不能为空")
    @Size(max = 3000, message = "工作内容不能超过3000个字符")
    private String workContent;

    @NotBlank(message = "完成情况不能为空")
    @Size(max = 1000, message = "完成情况不能超过1000个字符")
    private String completionStatus;

    @Size(max = 3000, message = "问题描述不能超过3000个字符")
    private String problemDescription;

    @Size(max = 3000, message = "下一步计划不能超过3000个字符")
    private String nextPlan;

    @NotNull(message = "请指定团队")
    private Long teamId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getWorkContent() { return workContent; }
    public void setWorkContent(String workContent) { this.workContent = workContent; }
    public String getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(String completionStatus) { this.completionStatus = completionStatus; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String nextPlan) { this.nextPlan = nextPlan; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
