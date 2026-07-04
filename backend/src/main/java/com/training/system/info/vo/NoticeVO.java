package com.training.system.info.vo;

import java.time.LocalDateTime;

/**
 * 公告视图对象
 */
public class NoticeVO {

    private Long noticeId;
    private String title;
    private String content;
    private String type;
    private String attachPath;
    private Integer topFlag;
    private Long publisherId;
    private String publisherName;
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
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
