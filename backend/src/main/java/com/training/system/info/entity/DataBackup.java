package com.training.system.info.entity;

import java.time.LocalDateTime;

/**
 * 数据备份实体
 * 对应数据库表 data_backup
 */
public class DataBackup {

    private Long backupId;
    private LocalDateTime backupTime;
    private String filePath;
    private String fileSize;
    private Long operatorId;

    public Long getBackupId() { return backupId; }
    public void setBackupId(Long backupId) { this.backupId = backupId; }
    public LocalDateTime getBackupTime() { return backupTime; }
    public void setBackupTime(LocalDateTime backupTime) { this.backupTime = backupTime; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
}
