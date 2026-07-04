package com.training.system.selection.vo;

import java.time.LocalDateTime;

public class TopicVO {
    private Long id;
    private String title;
    private String description;
    private String direction;
    private String difficulty;
    private Long teacherId;
    private Integer minMembers;
    private Integer maxMembers;
    private String status;
    private LocalDateTime selectionStart;
    private LocalDateTime selectionEnd;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSelectionStart() { return selectionStart; }
    public void setSelectionStart(LocalDateTime selectionStart) { this.selectionStart = selectionStart; }
    public LocalDateTime getSelectionEnd() { return selectionEnd; }
    public void setSelectionEnd(LocalDateTime selectionEnd) { this.selectionEnd = selectionEnd; }
}
