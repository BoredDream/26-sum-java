package com.training.system.topic.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情视图对象
 */
public class TopicDetailVO {

    private Long topicId;
    private String topicName;
    private String topicType;
    private String difficulty;
    private Long teacherId;
    private String teacherName;
    private String teacherNo;
    private Integer studentLimit;
    private Integer teamLimit;
    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;
    private String topicContent;
    private String developTools;
    private String technicalRoute;
    private Integer status;
    private String statusText;
    private Integer openStatus;
    private String openStatusText;
    private List<TopicFileVO> files;
    private List<TopicReviewVO> reviews;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(Integer openStatus) {
        this.openStatus = openStatus;
    }

    public String getOpenStatusText() {
        return openStatusText;
    }

    public void setOpenStatusText(String openStatusText) {
        this.openStatusText = openStatusText;
    }

    public List<TopicFileVO> getFiles() {
        return files;
    }

    public void setFiles(List<TopicFileVO> files) {
        this.files = files;
    }

    public List<TopicReviewVO> getReviews() {
        return reviews;
    }

    public void setReviews(List<TopicReviewVO> reviews) {
        this.reviews = reviews;
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
}
