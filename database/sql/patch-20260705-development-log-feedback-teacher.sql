USE training_selection_system;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'development_log'
    AND COLUMN_NAME = 'feedback_teacher_id'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE development_log ADD COLUMN feedback_teacher_id BIGINT NULL COMMENT ''反馈教师编号'' AFTER teacher_feedback',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
