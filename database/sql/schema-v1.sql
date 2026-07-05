-- 基于 Java 的综合实训选题系统
-- 数据库总表结构 V1
-- MySQL 8.x / InnoDB / utf8mb4

CREATE DATABASE IF NOT EXISTS training_selection_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE training_selection_system;
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS code_commit;
DROP TABLE IF EXISTS code_repository;
DROP TABLE IF EXISTS score_detail;
DROP TABLE IF EXISTS student_score;
DROP TABLE IF EXISTS score;
DROP TABLE IF EXISTS ai_report;
DROP TABLE IF EXISTS contribution;
DROP TABLE IF EXISTS stage_evaluation;
DROP TABLE IF EXISTS stage_task;
DROP TABLE IF EXISTS operate_log;
DROP TABLE IF EXISTS data_backup;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS makeup_sign_apply;
DROP TABLE IF EXISTS attendance_record;
DROP TABLE IF EXISTS attendance_task;
DROP TABLE IF EXISTS development_log;
DROP TABLE IF EXISTS process_document;
DROP TABLE IF EXISTS topic_selection;
DROP TABLE IF EXISTS team_join_request;
DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS team_info;
DROP TABLE IF EXISTS topic_review;
DROP TABLE IF EXISTS topic_file;
DROP TABLE IF EXISTS project_topic;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS student;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE student (
  student_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生编号',
  student_no VARCHAR(30) NOT NULL UNIQUE COMMENT '学号',
  student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
  major VARCHAR(100) NOT NULL COMMENT '专业',
  class_name VARCHAR(100) NOT NULL COMMENT '班级',
  phone VARCHAR(20) NULL COMMENT '手机号',
  email VARCHAR(100) NULL COMMENT '邮箱',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

CREATE TABLE teacher (
  teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师编号',
  teacher_no VARCHAR(30) NOT NULL UNIQUE COMMENT '工号',
  teacher_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
  office VARCHAR(100) NULL COMMENT '教研室',
  title VARCHAR(50) NULL COMMENT '职称',
  phone VARCHAR(20) NULL COMMENT '手机号',
  email VARCHAR(100) NULL COMMENT '邮箱',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

CREATE TABLE user_account (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户账号编号',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号，学号或工号',
  password VARCHAR(100) NOT NULL COMMENT '加密后的登录密码',
  role VARCHAR(20) NOT NULL COMMENT '角色：STUDENT、TEACHER、ADMIN',
  related_id BIGINT NOT NULL COMMENT '关联 student_id 或 teacher_id，管理员可关联教师编号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常，0-禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_role_related (role, related_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

CREATE TABLE project_topic (
  topic_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目编号',
  topic_name VARCHAR(100) NOT NULL COMMENT '项目题目名称',
  topic_type VARCHAR(50) NOT NULL COMMENT '项目类型',
  difficulty VARCHAR(20) NOT NULL COMMENT '项目难度',
  teacher_id BIGINT NOT NULL COMMENT '指导教师编号',
  student_limit INT NOT NULL COMMENT '单个团队人数上限',
  selection_start_time DATETIME NULL COMMENT '选题开始时间',
  selection_end_time DATETIME NULL COMMENT '选题截止时间',
  team_limit INT NULL COMMENT '允许选择该题目的最大团队数，空表示不限制',
  topic_content TEXT NOT NULL COMMENT '项目内容及要求',
  develop_tools VARCHAR(200) NULL COMMENT '开发工具与软件',
  technical_route TEXT NOT NULL COMMENT '技术路线',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-草稿，1-待审核，2-审核通过，3-退回修改，4-不予通过',
  open_status TINYINT NOT NULL DEFAULT 0 COMMENT '开放状态：0-未开放，1-已开放，2-已关闭',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  delete_time DATETIME NULL COMMENT '删除时间',
  CONSTRAINT fk_project_topic_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_topic_teacher (teacher_id),
  INDEX idx_topic_status (status, open_status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目题目表';

CREATE TABLE topic_file (
  file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件编号',
  topic_id BIGINT NOT NULL COMMENT '所属题目编号',
  file_name VARCHAR(200) NOT NULL COMMENT '文件名称',
  file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
  file_size BIGINT NOT NULL COMMENT '文件大小',
  upload_user_id BIGINT NOT NULL COMMENT '上传人账号编号',
  upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  file_desc VARCHAR(255) NULL COMMENT '文件说明',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  CONSTRAINT fk_topic_file_topic FOREIGN KEY (topic_id) REFERENCES project_topic(topic_id),
  CONSTRAINT fk_topic_file_user FOREIGN KEY (upload_user_id) REFERENCES user_account(user_id),
  INDEX idx_topic_file_topic (topic_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目资料文件表';

CREATE TABLE topic_review (
  review_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核记录编号',
  topic_id BIGINT NOT NULL COMMENT '被审核题目编号',
  admin_id BIGINT NOT NULL COMMENT '审核管理员账号编号',
  review_result TINYINT NOT NULL COMMENT '审核结果：1-通过，2-退回修改，3-不予通过',
  review_comment TEXT NOT NULL COMMENT '审核意见',
  review_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT fk_topic_review_topic FOREIGN KEY (topic_id) REFERENCES project_topic(topic_id),
  CONSTRAINT fk_topic_review_admin FOREIGN KEY (admin_id) REFERENCES user_account(user_id),
  INDEX idx_topic_review_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目审核记录表';

CREATE TABLE team_info (
  team_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队编号',
  team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
  leader_id BIGINT NOT NULL COMMENT '团队负责人学生编号',
  team_intro VARCHAR(500) NULL COMMENT '团队简介',
  team_status TINYINT NOT NULL DEFAULT 0 COMMENT '团队状态：0-组建中，1-待选题，2-已选题，3-已解散',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES student(student_id),
  INDEX idx_team_leader (leader_id),
  INDEX idx_team_status (team_status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队信息表';

CREATE TABLE team_member (
  member_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队成员编号',
  team_id BIGINT NOT NULL COMMENT '所属团队编号',
  student_id BIGINT NOT NULL COMMENT '学生编号',
  member_role TINYINT NOT NULL DEFAULT 0 COMMENT '成员角色：1-负责人，0-普通成员',
  work_content VARCHAR(500) NULL COMMENT '成员分工内容',
  join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-退出',
  CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_team_member_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_team_student (team_id, student_id),
  INDEX idx_team_member_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

CREATE TABLE team_join_request (
  request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入队申请编号',
  team_id BIGINT NOT NULL COMMENT '申请加入的团队编号',
  applicant_id BIGINT NOT NULL COMMENT '申请学生编号',
  apply_message VARCHAR(500) NULL COMMENT '申请说明',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-驳回',
  reviewer_id BIGINT NULL COMMENT '审核人学生编号（团队负责人）',
  review_opinion VARCHAR(500) NULL COMMENT '审核意见',
  apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  review_time DATETIME NULL COMMENT '审核时间',
  CONSTRAINT fk_join_request_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_join_request_applicant FOREIGN KEY (applicant_id) REFERENCES student(student_id),
  CONSTRAINT fk_join_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES student(student_id),
  INDEX idx_join_request_team_status (team_id, audit_status),
  INDEX idx_join_request_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队入队申请表';

CREATE TABLE topic_selection (
  selection_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选题申请编号',
  team_id BIGINT NOT NULL COMMENT '申请团队编号',
  topic_id BIGINT NOT NULL COMMENT '申请题目编号',
  selection_reason VARCHAR(1000) NOT NULL COMMENT '选题说明',
  apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-驳回，3-已撤回',
  audit_teacher_id BIGINT NULL COMMENT '审核教师编号',
  audit_opinion VARCHAR(1000) NULL COMMENT '审核意见',
  audit_time DATETIME NULL COMMENT '审核时间',
  CONSTRAINT fk_selection_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_selection_topic FOREIGN KEY (topic_id) REFERENCES project_topic(topic_id),
  CONSTRAINT fk_selection_teacher FOREIGN KEY (audit_teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_selection_team_status (team_id, audit_status),
  INDEX idx_selection_topic_status (topic_id, audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选题申请表';

CREATE TABLE process_document (
  document_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '过程文档编号',
  team_id BIGINT NOT NULL COMMENT '所属团队编号',
  topic_id BIGINT NOT NULL COMMENT '所属课题编号',
  document_name VARCHAR(200) NOT NULL COMMENT '文档名称',
  document_type VARCHAR(50) NOT NULL COMMENT '文档类型',
  project_stage VARCHAR(50) NOT NULL COMMENT '所属项目阶段',
  file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  file_size BIGINT NULL COMMENT '文件大小',
  version_no VARCHAR(20) NOT NULL COMMENT '文档版本号',
  uploader_id BIGINT NOT NULL COMMENT '上传学生编号',
  upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-需修改',
  feedback_teacher_id BIGINT NULL COMMENT '反馈教师编号',
  teacher_feedback VARCHAR(1000) NULL COMMENT '教师反馈意见',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_process_document_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_process_document_topic FOREIGN KEY (topic_id) REFERENCES project_topic(topic_id),
  CONSTRAINT fk_process_document_student FOREIGN KEY (uploader_id) REFERENCES student(student_id),
  CONSTRAINT fk_process_document_teacher FOREIGN KEY (feedback_teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_document_team_stage (team_id, project_stage, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='过程文档表';

CREATE TABLE development_log (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '开发日志编号',
  team_id BIGINT NOT NULL COMMENT '所属团队编号',
  student_id BIGINT NOT NULL COMMENT '日志提交学生编号',
  log_title VARCHAR(200) NOT NULL COMMENT '日志标题',
  log_date DATE NOT NULL COMMENT '日志日期',
  work_content TEXT NOT NULL COMMENT '工作内容',
  completion_status VARCHAR(500) NOT NULL COMMENT '完成情况',
  problem_description TEXT NULL COMMENT '遇到的问题',
  next_plan TEXT NULL COMMENT '下一步计划',
  teacher_feedback TEXT NULL COMMENT '教师反馈',
  feedback_teacher_id BIGINT NULL COMMENT '反馈教师编号',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_log_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_log_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  CONSTRAINT fk_log_feedback_teacher FOREIGN KEY (feedback_teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_log_team_date (team_id, log_date, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开发日志表';

CREATE TABLE attendance_task (
  task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '签到任务编号',
  task_title VARCHAR(100) NOT NULL COMMENT '签到任务标题',
  task_type TINYINT NOT NULL COMMENT '签到类型：1-日常签到，2-阶段签到',
  scope_type TINYINT NOT NULL DEFAULT 1 COMMENT '适用范围类型：1-班级，2-团队，3-全部',
  scope_value VARCHAR(100) NULL COMMENT '适用范围值，如班级名称或团队ID',
  start_time DATETIME NOT NULL COMMENT '签到开始时间',
  end_time DATETIME NOT NULL COMMENT '签到结束时间',
  teacher_id BIGINT NOT NULL COMMENT '发布教师ID',
  description TEXT NULL COMMENT '签到说明',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_attendance_task_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_attendance_task_time (start_time, end_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到任务表';

CREATE TABLE attendance_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '签到记录编号',
  task_id BIGINT NOT NULL COMMENT '签到任务ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  sign_time DATETIME NULL COMMENT '签到时间',
  sign_status TINYINT NOT NULL DEFAULT 0 COMMENT '签到状态：0-缺勤，1-正常，2-迟到，3-补签',
  remark VARCHAR(255) NULL COMMENT '签到备注',
  is_makeup TINYINT NOT NULL DEFAULT 0 COMMENT '是否补签：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_attendance_record_task FOREIGN KEY (task_id) REFERENCES attendance_task(task_id),
  CONSTRAINT fk_attendance_record_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_attendance_task_student (task_id, student_id),
  INDEX idx_attendance_record_student (student_id, sign_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

CREATE TABLE makeup_sign_apply (
  apply_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '补签申请编号',
  task_id BIGINT NOT NULL COMMENT '签到任务ID',
  record_id BIGINT NULL COMMENT '关联签到记录ID，可为空',
  student_id BIGINT NOT NULL COMMENT '申请学生ID',
  apply_reason TEXT NOT NULL COMMENT '补签原因',
  proof_file_path VARCHAR(500) NULL COMMENT '证明材料路径',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-驳回',
  audit_teacher_id BIGINT NULL COMMENT '审核教师ID',
  audit_comment TEXT NULL COMMENT '审核意见',
  audit_time DATETIME NULL COMMENT '审核时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_makeup_task FOREIGN KEY (task_id) REFERENCES attendance_task(task_id),
  CONSTRAINT fk_makeup_record FOREIGN KEY (record_id) REFERENCES attendance_record(record_id),
  CONSTRAINT fk_makeup_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  CONSTRAINT fk_makeup_teacher FOREIGN KEY (audit_teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_makeup_student_status (student_id, audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补签申请表';

CREATE TABLE notice (
  notice_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告编号',
  title VARCHAR(100) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告正文',
  type VARCHAR(20) NOT NULL COMMENT '公告分类',
  attach_path VARCHAR(200) NULL COMMENT '附件存储路径',
  top_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0-普通，1-置顶',
  publisher_id BIGINT NOT NULL COMMENT '发布人账号编号',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0-否，1-是',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_notice_publisher FOREIGN KEY (publisher_id) REFERENCES user_account(user_id),
  INDEX idx_notice_type_deleted (type, is_deleted, top_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE data_backup (
  backup_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '备份记录编号',
  backup_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '备份执行时间',
  file_path VARCHAR(200) NOT NULL COMMENT '备份文件存储路径',
  file_size VARCHAR(20) NOT NULL COMMENT '文件大小',
  operator_id BIGINT NOT NULL COMMENT '操作管理员账号编号',
  CONSTRAINT fk_backup_operator FOREIGN KEY (operator_id) REFERENCES user_account(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据备份表';

CREATE TABLE operate_log (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作日志编号',
  operate_user_id BIGINT NOT NULL COMMENT '操作人账号编号',
  operate_type VARCHAR(30) NOT NULL COMMENT '操作类型',
  operate_content TEXT NULL COMMENT '操作详情描述',
  operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  CONSTRAINT fk_operate_log_user FOREIGN KEY (operate_user_id) REFERENCES user_account(user_id),
  INDEX idx_operate_log_user_time (operate_user_id, operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE stage_task (
  stage_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '阶段任务编号',
  stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
  stage_desc TEXT NOT NULL COMMENT '阶段描述',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '截止时间',
  deliverables TEXT NOT NULL COMMENT '交付物要求',
  scoring_criteria TEXT NOT NULL COMMENT '评分标准',
  weight DECIMAL(5,2) NOT NULL COMMENT '权重占比',
  teacher_id BIGINT NOT NULL COMMENT '发布教师ID',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_stage_task_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阶段任务表';

CREATE TABLE stage_evaluation (
  evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '阶段评价编号',
  stage_id BIGINT NOT NULL COMMENT '阶段任务ID',
  team_id BIGINT NOT NULL COMMENT '队伍ID',
  related_document_id BIGINT NULL COMMENT '关联过程文档ID',
  doc_score DECIMAL(5,2) NOT NULL COMMENT '文档质量得分',
  completion_score DECIMAL(5,2) NOT NULL COMMENT '完成度得分',
  innovation_score DECIMAL(5,2) NULL COMMENT '创新性得分',
  tech_score DECIMAL(5,2) NULL COMMENT '技术难度得分',
  total_score DECIMAL(5,2) NOT NULL COMMENT '综合得分',
  comment TEXT NOT NULL COMMENT '评价意见',
  result TINYINT NOT NULL COMMENT '评价结果：1-通过，2-需修改，3-不通过',
  teacher_id BIGINT NOT NULL COMMENT '评价教师ID',
  is_late TINYINT NOT NULL DEFAULT 0 COMMENT '是否迟交：0-否，1-是',
  late_days INT NULL COMMENT '迟交天数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_stage_eval_stage FOREIGN KEY (stage_id) REFERENCES stage_task(stage_id),
  CONSTRAINT fk_stage_eval_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_stage_eval_document FOREIGN KEY (related_document_id) REFERENCES process_document(document_id),
  CONSTRAINT fk_stage_eval_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
  INDEX idx_stage_eval_team (team_id, stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阶段评价表';

CREATE TABLE contribution (
  contribution_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '贡献度记录编号',
  team_id BIGINT NOT NULL COMMENT '队伍ID',
  stage_id BIGINT NULL COMMENT '阶段ID，空表示整体',
  student_id BIGINT NOT NULL COMMENT '被评价学生ID',
  evaluator_id BIGINT NOT NULL COMMENT '评价人账号编号',
  evaluation_type TINYINT NOT NULL COMMENT '评价类型：1-互评，2-教师评价，3-自评',
  contribution_score DECIMAL(5,2) NOT NULL COMMENT '贡献度评分',
  workload_ratio DECIMAL(5,2) NOT NULL COMMENT '工作量占比',
  work_description TEXT NOT NULL COMMENT '工作内容描述',
  comment TEXT NULL COMMENT '评价说明',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_contribution_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_contribution_stage FOREIGN KEY (stage_id) REFERENCES stage_task(stage_id),
  CONSTRAINT fk_contribution_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  CONSTRAINT fk_contribution_evaluator FOREIGN KEY (evaluator_id) REFERENCES user_account(user_id),
  INDEX idx_contribution_team_student (team_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贡献度表';

CREATE TABLE ai_report (
  report_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'AI报告编号',
  team_id BIGINT NOT NULL COMMENT '所属团队编号',
  summary TEXT NOT NULL COMMENT '项目完成情况总结',
  doc_analysis TEXT NULL COMMENT '文档质量分析',
  code_analysis TEXT NULL COMMENT '代码实现分析',
  log_analysis TEXT NULL COMMENT '开发日志分析',
  attendance_analysis TEXT NULL COMMENT '考勤情况分析',
  problem TEXT NULL COMMENT '存在问题',
  suggestion TEXT NULL COMMENT '改进建议',
  ai_score DECIMAL(5,2) NULL COMMENT 'AI建议总分',
  version_no INT NOT NULL DEFAULT 1 COMMENT '报告版本号',
  is_visible_to_student TINYINT NOT NULL DEFAULT 1 COMMENT '是否学生可见：1-可见，0-不可见',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
  CONSTRAINT fk_ai_report_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  INDEX idx_ai_report_team (team_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI分析报告表';

CREATE TABLE score (
  score_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团队成绩编号',
  team_id BIGINT NOT NULL COMMENT '队伍ID',
  teacher_id BIGINT NOT NULL COMMENT '评分教师ID',
  ai_report_id BIGINT NULL COMMENT '关联AI报告ID',
  doc_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '文档编写成绩',
  attendance_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '考勤成绩',
  system_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '系统实现与测试成绩',
  defense_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '答辩成绩',
  total_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '团队总成绩',
  teacher_comment TEXT NULL COMMENT '教师总评',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已确认，2-已锁定',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_score_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_score_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
  CONSTRAINT fk_score_ai_report FOREIGN KEY (ai_report_id) REFERENCES ai_report(report_id),
  UNIQUE KEY uk_score_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成绩主表';

CREATE TABLE student_score (
  student_score_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '个人成绩编号',
  score_id BIGINT NOT NULL COMMENT '关联团队成绩ID',
  team_id BIGINT NOT NULL COMMENT '队伍ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  contribution_factor DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '贡献度调整系数',
  personal_score DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '个人最终成绩',
  grade VARCHAR(10) NULL COMMENT '成绩等级',
  teacher_comment TEXT NULL COMMENT '个人成绩评语',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_student_score_score FOREIGN KEY (score_id) REFERENCES score(score_id),
  CONSTRAINT fk_student_score_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_student_score_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_score_student (score_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人成绩表';

CREATE TABLE score_detail (
  detail_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分明细编号',
  score_id BIGINT NOT NULL COMMENT '所属团队成绩编号',
  item_name VARCHAR(100) NOT NULL COMMENT '评分项名称',
  full_score DECIMAL(5,2) NOT NULL COMMENT '满分',
  ai_suggest_score DECIMAL(5,2) NULL COMMENT 'AI建议分',
  teacher_score DECIMAL(5,2) NOT NULL COMMENT '教师确认分',
  reason VARCHAR(500) NULL COMMENT '评分说明或扣分原因',
  CONSTRAINT fk_score_detail_score FOREIGN KEY (score_id) REFERENCES score(score_id),
  INDEX idx_score_detail_score (score_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分明细表';

CREATE TABLE code_repository (
  repo_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Git仓库编号',
  team_id BIGINT NOT NULL COMMENT '团队编号',
  repo_url VARCHAR(500) NOT NULL COMMENT 'Git仓库地址',
  branch_name VARCHAR(100) NOT NULL DEFAULT 'main' COMMENT '默认分析分支',
  submit_user_id BIGINT NOT NULL COMMENT '提交仓库地址的学生编号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_repo_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_repo_submit_student FOREIGN KEY (submit_user_id) REFERENCES student(student_id),
  UNIQUE KEY uk_repo_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Git仓库信息表';

CREATE TABLE code_commit (
  commit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Git提交记录编号',
  repo_id BIGINT NOT NULL COMMENT 'Git仓库编号',
  team_id BIGINT NOT NULL COMMENT '团队编号',
  student_id BIGINT NULL COMMENT '匹配到的学生编号，无法匹配时可为空',
  commit_hash VARCHAR(100) NOT NULL COMMENT 'Git commit hash',
  author_name VARCHAR(100) NOT NULL COMMENT 'Git提交作者名称',
  author_email VARCHAR(100) NULL COMMENT 'Git提交作者邮箱',
  commit_message VARCHAR(500) NULL COMMENT 'Git提交说明',
  commit_time DATETIME NOT NULL COMMENT 'Git提交时间',
  changed_files INT NULL COMMENT '修改文件数量',
  additions INT NULL COMMENT '新增行数',
  deletions INT NULL COMMENT '删除行数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  CONSTRAINT fk_commit_repo FOREIGN KEY (repo_id) REFERENCES code_repository(repo_id),
  CONSTRAINT fk_commit_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_commit_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_repo_commit_hash (repo_id, commit_hash),
  INDEX idx_commit_team_time (team_id, commit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Git提交记录表';
