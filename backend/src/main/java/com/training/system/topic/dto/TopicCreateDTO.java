package com.training.system.topic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 新增题目请求参数
 */
public class TopicCreateDTO {

    @NotBlank(message = "项目名称不能为空")
    private String topicName;

    @NotBlank(message = "项目类型不能为空")
    private String topicType;

    @NotBlank(message = "项目难度不能为空")
    private String difficulty;

    @NotNull(message = "请填写单个团队人数上限")
    @Min(value = 1, message = "学生人数必须大于0")
    private Integer studentLimit;

    private Integer teamLimit;

    @NotBlank(message = "项目内容及要求不能为空")
    private String topicContent;

    private String developTools;

    @NotBlank(message = "技术路线不能为空")
    private String technicalRoute;

    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;

    @NotNull(message = "请指定操作类型")
    private Integer status;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicType() {
        return topicType;
    }

    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getStudentLimit() {
        return studentLimit;
    }

    public void setStudentLimit(Integer studentLimit) {
        this.studentLimit = studentLimit;
    }

    public Integer getTeamLimit() {
        return teamLimit;
    }

    public void setTeamLimit(Integer teamLimit) {
        this.teamLimit = teamLimit;
    }

    public String getTopicContent() {
        return topicContent;
    }

    public void setTopicContent(String topicContent) {
        this.topicContent = topicContent;
    }

    public String getDevelopTools() {
        return developTools;
    }

    public void setDevelopTools(String developTools) {
        this.developTools = developTools;
    }

    public String getTechnicalRoute() {
        return technicalRoute;
    }

    public void setTechnicalRoute(String technicalRoute) {
        this.technicalRoute = technicalRoute;
    }

    public LocalDateTime getSelectionStartTime() {
        return selectionStartTime;
    }

    public void setSelectionStartTime(LocalDateTime selectionStartTime) {
        this.selectionStartTime = selectionStartTime;
    }

    public LocalDateTime getSelectionEndTime() {
        return selectionEndTime;
    }

    public void setSelectionEndTime(LocalDateTime selectionEndTime) {
        this.selectionEndTime = selectionEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
