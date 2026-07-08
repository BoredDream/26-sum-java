-- 补丁：为 attendance_task 表补充定位签到相关字段
-- 适用版本：schema-v1.sql
-- 问题背景：代码和前端已实现定位签到功能，但数据库表缺少对应字段，
--         导致发布定位签到任务时 SQL 报错 "Unknown column"，返回系统异常。

ALTER TABLE attendance_task
    ADD COLUMN require_location TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用定位签到：0-否，1-是',
    ADD COLUMN location_lng DECIMAL(10, 7) NULL COMMENT '签到位置经度',
    ADD COLUMN location_lat DECIMAL(10, 7) NULL COMMENT '签到位置纬度',
    ADD COLUMN location_radius INT NULL DEFAULT 500 COMMENT '签到允许半径（米），默认500米',
    ADD COLUMN location_name VARCHAR(255) NULL COMMENT '签到位置名称';
