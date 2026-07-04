package com.training.system.info.entity;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应数据库表 operate_log
 */
public class OperateLog {

    private Long logId;
    private Long operateUserId;
    private String operateType;
    private String operateContent;
    private LocalDateTime operateTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public String getOperateType() { return operateType; }
    public void setOperateType(String operateType) { this.operateType = operateType; }
    public Long getOperateUserId() { return operateUserId; }
    public void setOperateUserId(Long operateUserId) { this.operateUserId = operateUserId; }
    public String getOperateContent() { return operateContent; }
    public void setOperateContent(String operateContent) { this.operateContent = operateContent; }
    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
}
