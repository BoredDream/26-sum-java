package com.training.system.selection.entity;

import java.time.LocalDateTime;

public class TeamEntity {
    private Long id;
    private String teamName;
    private Long leaderId;
    private String introduction;
    private String status;
    private Long selectedTopicId;
    private Integer maxSize;
    private Integer memberCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSelectedTopicId() { return selectedTopicId; }
    public void setSelectedTopicId(Long selectedTopicId) { this.selectedTopicId = selectedTopicId; }
    public Integer getMaxSize() { return maxSize; }
    public void setMaxSize(Integer maxSize) { this.maxSize = maxSize; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
