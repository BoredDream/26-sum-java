package com.training.system.info.dto;

/**
 * 公告查询请求参数
 */
public class NoticeQueryDTO {

    private String keyword;
    private String type;
    private Integer pageNum = 1;
    private Integer pageSize = 15;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
