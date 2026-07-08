-- 签到任务新增定位签到字段
ALTER TABLE attendance_task ADD COLUMN require_location TINYINT(1) DEFAULT 0 COMMENT '是否要求定位签到 (0=否, 1=是)';
ALTER TABLE attendance_task ADD COLUMN location_lng    DECIMAL(11,7) NULL COMMENT '签到中心经度 (GCJ-02)';
ALTER TABLE attendance_task ADD COLUMN location_lat    DECIMAL(10,7) NULL COMMENT '签到中心纬度 (GCJ-02)';
ALTER TABLE attendance_task ADD COLUMN location_radius INT DEFAULT 500 COMMENT '允许签到半径 (米)';
ALTER TABLE attendance_task ADD COLUMN location_name   VARCHAR(100) NULL COMMENT '签到地点名称';
