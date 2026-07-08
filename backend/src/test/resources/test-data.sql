-- 测试数据
-- 教师
INSERT INTO teacher (teacher_id, teacher_no, teacher_name) VALUES (10, 'T001', '张老师');
INSERT INTO teacher (teacher_id, teacher_no, teacher_name) VALUES (11, 'T002', '李老师');

-- 学生
INSERT INTO student (student_id, student_no, student_name, major, class_name) VALUES
(100, 'S001', '张三', '软件工程', '软件工程2023级1班'),
(101, 'S002', '李四', '软件工程', '软件工程2023级1班'),
(102, 'S003', '王五', '软件工程', '软件工程2023级2班');

-- 团队
INSERT INTO team_info (team_id, team_name, leader_id) VALUES (1, '测试团队1', 100);

-- 团队成员
INSERT INTO team_member (member_id, team_id, student_id, member_role, status) VALUES
(1, 1, 100, 1, 1),
(2, 1, 101, 0, 1);

-- 签到任务
INSERT INTO attendance_task (task_id, task_title, task_type, scope_type, scope_value, start_time, end_time, teacher_id, status) VALUES
(1, '每日签到-全部', 1, 3, NULL, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('HOUR', 1, CURRENT_TIMESTAMP), 10, 1),
(2, '阶段签到-班级', 2, 1, '软件工程2023级1班', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', 1, CURRENT_TIMESTAMP), 10, 1);

-- 考勤记录
INSERT INTO attendance_record (record_id, task_id, student_id, sign_status, is_makeup) VALUES
(1, 1, 100, 1, 0),
(2, 1, 101, 0, 0),
(3, 1, 102, 2, 0);
