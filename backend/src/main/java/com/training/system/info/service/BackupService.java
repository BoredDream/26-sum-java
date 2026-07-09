package com.training.system.info.service;

import com.training.system.common.PageResult;
import com.training.system.info.vo.BackupVO;

/**
 * 数据备份服务接口
 */
public interface BackupService {

    BackupVO manualBackup(Long operatorId);

    PageResult<BackupVO> pageBackups(int pageNum, int pageSize);

    String getFilePath(Long backupId);

    void restoreBackup(Long backupId);

    void deleteBackup(Long backupId);

    void cleanupOldBackups();

    long count();

    boolean isRestoring();

    String getRestoreMessage();
}
