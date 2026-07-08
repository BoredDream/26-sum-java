package com.training.system.info.vo;

import java.time.LocalDateTime;

/**
 * 操作日志视图对象
 */
public class OperateLogVO {

    private Long logId;
    private Long operateUserId;
    private String operateUserName;
    private String operateType;
    private String operateContent;
    private LocalDateTime operateTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getOperateUserId() { return operateUserId; }
    public void setOperateUserId(Long operateUserId) { this.operateUserId = operateUserId; }
    public String getOperateUserName() { return operateUserName; }
    public void setOperateUserName(String operateUserName) { this.operateUserName = operateUserName; }
    public String getOperateType() { return operateType; }
    public void setOperateType(String operateType) { this.operateType = operateType; }
    public String getOperateContent() { return operateContent; }
    public void setOperateContent(String operateContent) { this.operateContent = operateContent; }
    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
}
