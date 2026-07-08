package com.training.system.topic.entity;

import java.time.LocalDateTime;

/**
 * 项目题目实体
 * 对应数据库表 project_topic
 */
public class ProjectTopic {

    private Long topicId;
    private String topicName;
    private String topicType;
    private String difficulty;
    private Long teacherId;
    private Integer studentLimit;
    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;
    private Integer teamLimit;
    private String topicContent;
    private String developTools;
    private String technicalRoute;
    private Integer status;
    private Integer openStatus;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

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

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getStudentLimit() {
        return studentLimit;
    }

    public void setStudentLimit(Integer studentLimit) {
        this.studentLimit = studentLimit;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(Integer openStatus) {
        this.openStatus = openStatus;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }
}
