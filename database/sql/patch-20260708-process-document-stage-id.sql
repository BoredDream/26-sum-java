USE training_selection_system;

-- 为已存在的 process_document 表补充 stage_id 和 remark 字段
-- 该字段在 schema-v1.sql 后续版本中已加入，旧数据库通过此补丁迁移

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'process_document'
    AND COLUMN_NAME = 'stage_id'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE process_document ADD COLUMN stage_id BIGINT NULL COMMENT ''关联阶段任务ID'' AFTER teacher_feedback',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'process_document'
    AND COLUMN_NAME = 'remark'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE process_document ADD COLUMN remark VARCHAR(500) NULL COMMENT ''备注'' AFTER stage_id',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
