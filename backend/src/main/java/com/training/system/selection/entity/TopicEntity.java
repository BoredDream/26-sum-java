package com.training.system.selection.entity;

import java.time.LocalDateTime;

/** 课题实体由出题管理模块维护；选题模块仅查询并更新选题状态。 */
public class TopicEntity {
    private Long id;
    private String title;
    private String description;
    private String direction;
    private String difficulty;
    private Long teacherId;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer teamLimit;
    private String status;
    private LocalDateTime selectionStart;
    private LocalDateTime selectionEnd;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Integer getMinMembers() { return minMembers; }
    public void setMinMembers(Integer minMembers) { this.minMembers = minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public Integer getTeamLimit() { return teamLimit; }
    public void setTeamLimit(Integer teamLimit) { this.teamLimit = teamLimit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSelectionStart() { return selectionStart; }
    public void setSelectionStart(LocalDateTime selectionStart) { this.selectionStart = selectionStart; }
    public LocalDateTime getSelectionEnd() { return selectionEnd; }
    public void setSelectionEnd(LocalDateTime selectionEnd) { this.selectionEnd = selectionEnd; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
