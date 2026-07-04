USE training_selection_system;

-- 最小测试数据 V1
-- 密码字段使用占位哈希，正式开发时由登录模块统一加密生成。

INSERT INTO student (student_id, student_no, student_name, major, class_name, phone, email) VALUES
(1, '20260001', '张三', '软件工程', '软工2601', '13800000001', 'zhangsan@example.com'),
(2, '20260002', '李四', '软件工程', '软工2601', '13800000002', 'lisi@example.com'),
(3, '20260003', '王五', '软件工程', '软工2601', '13800000003', 'wangwu@example.com');

INSERT INTO teacher (teacher_id, teacher_no, teacher_name, office, title, phone, email) VALUES
(1, 'T2026001', '赵老师', '软件工程教研室', '讲师', '13900000001', 'zhao@example.com'),
(2, 'T2026002', '管理员', '综合实训办公室', '管理员', '13900000002', 'admin@example.com');

INSERT INTO user_account (user_id, username, password, role, related_id, status) VALUES
(1, '20260001', 'CHANGE_ME_HASH_123456', 'STUDENT', 1, 1),
(2, '20260002', 'CHANGE_ME_HASH_123456', 'STUDENT', 2, 1),
(3, '20260003', 'CHANGE_ME_HASH_123456', 'STUDENT', 3, 1),
(4, 'T2026001', 'CHANGE_ME_HASH_123456', 'TEACHER', 1, 1),
(5, 'admin', 'CHANGE_ME_HASH_admin123', 'ADMIN', 2, 1);

INSERT INTO project_topic (
  topic_id, topic_name, topic_type, difficulty, teacher_id, student_limit,
  selection_start_time, selection_end_time, team_limit, topic_content,
  develop_tools, technical_route, status, open_status
) VALUES
(1, '综合实训选题系统', '管理系统', '中', 1, 5,
 '2026-07-01 08:00:00', '2026-07-20 18:00:00', 3,
 '实现出题、选题、考勤、信息管理、成绩评估与过程分析。',
 'Java, Spring Boot, MySQL, MyBatis, Vue',
 '前后端分离，后端提供 REST JSON 接口。', 2, 1);

INSERT INTO team_info (team_id, team_name, leader_id, team_intro, team_status) VALUES
(1, '第一小组', 1, '综合实训示例团队', 1);

INSERT INTO team_member (member_id, team_id, student_id, member_role, work_content, status) VALUES
(1, 1, 1, 1, '项目负责人', 1),
(2, 1, 2, 0, '后端开发', 1),
(3, 1, 3, 0, '测试与文档', 1);

INSERT INTO topic_selection (selection_id, team_id, topic_id, selection_reason, audit_status, audit_teacher_id, audit_opinion, audit_time) VALUES
(1, 1, 1, '团队成员熟悉 Java 和数据库，适合完成该题目。', 1, 1, '同意选题。', NOW());

INSERT INTO attendance_task (task_id, task_title, task_type, scope_type, scope_value, start_time, end_time, teacher_id, description, status) VALUES
(1, '第1周签到', 1, 2, '1', '2026-07-04 08:00:00', '2026-07-04 08:30:00', 1, '综合实训第1周签到', 2);

INSERT INTO attendance_record (record_id, task_id, student_id, sign_time, sign_status, remark, is_makeup) VALUES
(1, 1, 1, '2026-07-04 08:05:00', 1, '正常签到', 0),
(2, 1, 2, '2026-07-04 08:20:00', 2, '迟到签到', 0),
(3, 1, 3, NULL, 0, '未签到', 0);

INSERT INTO notice (notice_id, title, content, type, publisher_id, top_flag) VALUES
(1, '综合实训启动通知', '请各小组阅读需求文档和开发规范，按模块提交接口清单。', '实训通知', 5, 1);
