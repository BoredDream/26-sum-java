USE training_selection_system;

-- 为已存在的 team_info 表补充 max_size 字段
-- 该字段在 schema-v1.sql 后续版本中已加入，旧数据库通过此补丁迁移
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'team_info'
    AND COLUMN_NAME = 'max_size'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE team_info ADD COLUMN max_size INT NOT NULL DEFAULT 6 COMMENT ''团队人数上限，默认6人'' AFTER team_status',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
