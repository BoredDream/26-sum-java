package com.training.system.info.entity;

import java.time.LocalDateTime;

/**
 * 公告实体
 * 对应数据库表 notice
 */
public class Notice {

    private Long noticeId;
    private String title;
    private String content;
    private String type;
    private String attachPath;
    private Integer topFlag;
    private Long publisherId;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAttachPath() { return attachPath; }
    public void setAttachPath(String attachPath) { this.attachPath = attachPath; }
    public Integer getTopFlag() { return topFlag; }
    public void setTopFlag(Integer topFlag) { this.topFlag = topFlag; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
