package com.training.system.info.dto;

/**
 * 更新公告请求参数
 */
public class NoticeUpdateDTO {

    private String title;
    private String content;
    private String type;
    private Integer topFlag;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getTopFlag() { return topFlag; }
    public void setTopFlag(Integer topFlag) { this.topFlag = topFlag; }
}
