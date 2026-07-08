package com.training.system.info.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增公告请求参数
 */
public class NoticeCreateDTO {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotBlank(message = "公告类型不能为空")
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
