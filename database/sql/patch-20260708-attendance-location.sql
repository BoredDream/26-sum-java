USE training_selection_system;

ALTER TABLE attendance_task
  ADD COLUMN require_location TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用定位签到',
  ADD COLUMN location_lng DECIMAL(10, 7) NULL COMMENT '签到位置经度',
  ADD COLUMN location_lat DECIMAL(10, 7) NULL COMMENT '签到位置纬度',
  ADD COLUMN location_radius INT NULL DEFAULT 500 COMMENT '签到允许半径（米）',
  ADD COLUMN location_name VARCHAR(255) NULL COMMENT '签到位置名称';
