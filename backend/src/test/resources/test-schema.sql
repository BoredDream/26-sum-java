-- H2 测试数据库表结构（兼容 MySQL 语法）
CREATE TABLE IF NOT EXISTS student (
  student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_no VARCHAR(30) NOT NULL UNIQUE,
  student_name VARCHAR(50) NOT NULL,
  major VARCHAR(100) NOT NULL,
  class_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NULL,
  email VARCHAR(100) NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS teacher (
  teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_no VARCHAR(30) NOT NULL UNIQUE,
  teacher_name VARCHAR(50) NOT NULL,
  office VARCHAR(100) NULL,
  title VARCHAR(50) NULL,
  phone VARCHAR(20) NULL,
  email VARCHAR(100) NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS attendance_task (
  task_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_title VARCHAR(100) NOT NULL,
  task_type TINYINT NOT NULL,
  scope_type TINYINT NOT NULL DEFAULT 1,
  scope_value VARCHAR(100) NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  teacher_id BIGINT NOT NULL,
  description TEXT NULL,
  require_location TINYINT NOT NULL DEFAULT 0,
  location_lng DECIMAL(10, 7) NULL,
  location_lat DECIMAL(10, 7) NULL,
  location_radius INT NULL DEFAULT 500,
  location_name VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL,
  FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS attendance_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  sign_time TIMESTAMP NULL,
  sign_status TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(255) NULL,
  is_makeup TINYINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL,
  FOREIGN KEY (task_id) REFERENCES attendance_task(task_id),
  FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_attendance_task_student (task_id, student_id)
);

CREATE TABLE IF NOT EXISTS makeup_sign_apply (
  apply_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  record_id BIGINT NULL,
  student_id BIGINT NOT NULL,
  apply_reason TEXT NOT NULL,
  proof_file_path VARCHAR(500) NULL,
  audit_status TINYINT NOT NULL DEFAULT 0,
  audit_teacher_id BIGINT NULL,
  audit_comment TEXT NULL,
  audit_time TIMESTAMP NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL,
  FOREIGN KEY (task_id) REFERENCES attendance_task(task_id),
  FOREIGN KEY (record_id) REFERENCES attendance_record(record_id),
  FOREIGN KEY (student_id) REFERENCES student(student_id),
  FOREIGN KEY (audit_teacher_id) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS team_info (
  team_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  team_name VARCHAR(100) NOT NULL,
  leader_id BIGINT NOT NULL,
  team_intro VARCHAR(500) NULL,
  team_status TINYINT NOT NULL DEFAULT 0,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NULL DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS team_member (
  member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  member_role TINYINT NOT NULL DEFAULT 0,
  work_content VARCHAR(500) NULL,
  join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1,
  FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  FOREIGN KEY (student_id) REFERENCES student(student_id),
  UNIQUE KEY uk_team_student (team_id, student_id)
);
