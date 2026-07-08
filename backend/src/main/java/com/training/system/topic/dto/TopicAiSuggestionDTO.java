package com.training.system.topic.dto;

import jakarta.validation.constraints.Size;

public class TopicAiSuggestionDTO {

    @Size(max = 100, message = "题目名称不能超过100个字符")
    private String topicName;

    @Size(max = 50, message = "题目类型不能超过50个字符")
    private String topicType;

    @Size(max = 20, message = "难度不能超过20个字符")
    private String difficulty;

    private Integer studentLimit;
    private Integer teamLimit;

    @Size(max = 2000, message = "题目内容不能超过2000个字符")
    private String topicContent;

    @Size(max = 1000, message = "技术路线不能超过1000个字符")
    private String technicalRoute;

    @Size(max = 500, message = "开发工具不能超过500个字符")
    private String developTools;

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

    public String getTechnicalRoute() {
        return technicalRoute;
    }

    public void setTechnicalRoute(String technicalRoute) {
        this.technicalRoute = technicalRoute;
    }

    public String getDevelopTools() {
        return developTools;
    }

    public void setDevelopTools(String developTools) {
        this.developTools = developTools;
    }
}
