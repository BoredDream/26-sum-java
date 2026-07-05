package com.training.system.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 发布签到任务请求参数
 */
public class AttendanceTaskCreateDTO {

    @NotBlank(message = "请输入签到标题")
    @Size(max = 100, message = "签到标题不超过100字符")
    private String taskTitle;

    @NotNull(message = "请选择签到类型")
    private Integer taskType;

    @NotNull(message = "请选择适用范围类型")
    private Integer scopeType;

    private String scopeValue;

    @NotNull(message = "请设置签到开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "请设置签到结束时间")
    private LocalDateTime endTime;

    private String description;

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Integer getScopeType() {
        return scopeType;
    }

    public void setScopeType(Integer scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
