package com.training.system.info.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.info.entity.DataBackup;
import com.training.system.info.exception.BusinessException;
import com.training.system.info.mapper.DataBackupMapper;
import com.training.system.info.service.BackupService;
import com.training.system.info.vo.BackupVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    @Autowired
    private DataBackupMapper backupMapper;
    @Autowired
    private DataSource dataSource;

    @Value("${backup.dir:./backup}")
    private String backupDir;
    @Value("${backup.retention-days:30}")
    private int retentionDays;

    private final AtomicBoolean restoring = new AtomicBoolean(false);

    @Override
    @Transactional
    public BackupVO manualBackup(Long operatorId) {
        return performBackup(operatorId);
    }

    @Scheduled(cron = "${backup.cron:0 0 2 * * ?}")
    @Transactional
    public void autoBackup() {
        log.info("执行自动备份任务...");
        try {
            performBackup(0L);
            log.info("自动备份完成");
        } catch (Exception e) {
            log.error("自动备份失败", e);
        }
    }

    private BackupVO performBackup(Long operatorId) {
        try {
            Files.createDirectories(Paths.get(backupDir));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "backup_" + timestamp + ".sql";
            Path filepath = Paths.get(backupDir, filename);

            exportDatabase(filepath);

            long fileSize = Files.size(filepath);

            DataBackup backup = new DataBackup();
            backup.setBackupTime(LocalDateTime.now());
            backup.setFilePath(filepath.toString());
            backup.setFileSize(readableFileSize(fileSize));
            backup.setOperatorId(operatorId);
            backupMapper.insert(backup);

            BackupVO vo = new BackupVO();
            vo.setBackupId(backup.getBackupId());
            vo.setBackupTime(backup.getBackupTime());
            vo.setFilePath(backup.getFilePath());
            vo.setFileSize(backup.getFileSize());
            vo.setOperatorId(backup.getOperatorId());
            return vo;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.ERROR, "备份失败：" + e.getMessage());
        }
    }

    private void exportDatabase(Path filepath) throws Exception {
        try (Connection conn = dataSource.getConnection();
             BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            writer.write("-- Database Backup -- Generated at " + LocalDateTime.now());
            writer.newLine();
            writer.write("SET NAMES utf8mb4;");
            writer.newLine();

            String[] tables = {"student", "teacher", "user_account", "notice", "data_backup", "operate_log"};
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
                    if (rs.next()) {
                        writer.write("DROP TABLE IF EXISTS `" + table + "`;");
                        writer.newLine();
                        writer.write(rs.getString(2) + ";");
                        writer.newLine();
                    }
                }
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "`")) {
                    int colCount = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder("INSERT INTO `" + table + "` VALUES (");
                        for (int i = 1; i <= colCount; i++) {
                            if (i > 1) sb.append(", ");
                            String val = rs.getString(i);
                            if (val == null) sb.append("NULL");
                            else sb.append("'").append(val.replace("\\", "\\\\").replace("'", "\\'")).append("'");
                        }
                        sb.append(");");
                        writer.write(sb.toString());
                        writer.newLine();
                    }
                }
                writer.newLine();
            }
        }
    }

    @Override
    public PageResult<BackupVO> pageBackups(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<BackupVO> records = backupMapper.selectPage(offset, pageSize);
        long total = backupMapper.countPage();
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public String getFilePath(Long backupId) {
        DataBackup backup = backupMapper.selectById(backupId);
        if (backup == null) throw new BusinessException(ResultCode.NOT_FOUND, "备份记录不存在");
        return backup.getFilePath();
    }

    @Override
    @Transactional
    public void restoreBackup(Long backupId) {
        if (!restoring.compareAndSet(false, true)) {
            throw new BusinessException(ResultCode.CONFLICT, "已有恢复操作正在进行中");
        }
        try {
            DataBackup backup = backupMapper.selectById(backupId);
            if (backup == null) throw new BusinessException(ResultCode.NOT_FOUND, "备份记录不存在");
            Path filepath = Paths.get(backup.getFilePath());
            if (!Files.exists(filepath)) throw new BusinessException(ResultCode.NOT_FOUND, "备份文件不存在");

            String sql = new String(Files.readAllBytes(filepath));
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                StringBuilder statementBuilder = new StringBuilder();
                for (String line : sql.split("\n")) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                    statementBuilder.append(line);
                    if (trimmed.endsWith(";")) {
                        try { stmt.execute(statementBuilder.toString().trim()); }
                        catch (Exception e) { log.warn("跳过SQL: {}", e.getMessage()); }
                        statementBuilder.setLength(0);
                    } else {
                        statementBuilder.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ResultCode.ERROR, "恢复失败：" + e.getMessage());
        } finally {
            restoring.set(false);
        }
    }

    @Override
    @Transactional
    public void deleteBackup(Long backupId) {
        DataBackup backup = backupMapper.selectById(backupId);
        if (backup != null) {
            try { Files.deleteIfExists(Paths.get(backup.getFilePath())); } catch (Exception ignored) {}
            backupMapper.deleteById(backupId);
        }
    }

    @Override
    @Transactional
    public void cleanupOldBackups() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<DataBackup> old = backupMapper.selectByTimeBefore(cutoff);
        for (DataBackup b : old) {
            try { Files.deleteIfExists(Paths.get(b.getFilePath())); } catch (Exception ignored) {}
            backupMapper.deleteById(b.getBackupId());
        }
    }

    @Override
    public long count() { return backupMapper.countPage(); }

    private String readableFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }
}
