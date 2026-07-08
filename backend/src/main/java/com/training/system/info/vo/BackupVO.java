package com.training.system.info.vo;

import java.time.LocalDateTime;

/**
 * 备份视图对象
 */
public class BackupVO {

    private Long backupId;
    private LocalDateTime backupTime;
    private String filePath;
    private String fileSize;
    private Long operatorId;
    private String operatorName;

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
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
