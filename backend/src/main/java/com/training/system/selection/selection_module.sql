-- 综合实训选题管理模块建表脚本
-- 说明：topic 表通常由“出题管理模块”维护；本模块只读取 topic 的课题信息和更新其选题状态。
-- 若你们数据库 v1 的 topic 字段名不同，只需调整 TopicMapper.java 中对应 SQL。

CREATE TABLE IF NOT EXISTS topic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(3000) NOT NULL,
    direction VARCHAR(100),
    difficulty VARCHAR(20),
    teacher_id BIGINT NOT NULL,
    min_members INT NOT NULL DEFAULT 1,
    max_members INT NOT NULL DEFAULT 6,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    selection_start DATETIME NULL,
    selection_end DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_topic_title (title),
    KEY idx_topic_teacher (teacher_id),
    KEY idx_topic_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题表（由出题管理模块维护）';

CREATE TABLE IF NOT EXISTS team (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_name VARCHAR(100) NOT NULL,
    leader_id BIGINT NOT NULL,
    introduction VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BUILDING',
    selected_topic_id BIGINT NULL,
    max_size INT NOT NULL DEFAULT 6,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_name (team_name),
    KEY idx_team_leader (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训团队表';

CREATE TABLE IF NOT EXISTS team_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    work_content VARCHAR(500) NULL,
    active TINYINT NOT NULL DEFAULT 1,
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_student (team_id, student_id),
    KEY idx_team_member_student_active (student_id, active),
    KEY idx_team_member_team_active (team_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

CREATE TABLE IF NOT EXISTS team_join_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    apply_message VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NULL,
    review_opinion VARCHAR(500) NULL,
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME NULL,
    KEY idx_team_join_request_team_status (team_id, status),
    KEY idx_team_join_request_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加入团队申请表';

CREATE TABLE IF NOT EXISTS topic_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    selection_reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    audit_teacher_id BIGINT NULL,
    audit_opinion VARCHAR(1000) NULL,
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME NULL,
    KEY idx_topic_selection_team (team_id),
    KEY idx_topic_selection_topic (topic_id),
    KEY idx_topic_selection_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选题申请表';

CREATE TABLE IF NOT EXISTS process_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    project_stage VARCHAR(50) NOT NULL,
    version_no VARCHAR(20) NOT NULL,
    original_filename VARCHAR(300) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    uploader_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    teacher_feedback VARCHAR(1000) NULL,
    feedback_teacher_id BIGINT NULL,
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    feedback_time DATETIME NULL,
    KEY idx_process_document_team (team_id),
    KEY idx_process_document_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='过程文档表';

CREATE TABLE IF NOT EXISTS development_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    log_date DATE NOT NULL,
    work_content VARCHAR(3000) NOT NULL,
    completion_status VARCHAR(1000) NOT NULL,
    problem_description VARCHAR(3000) NULL,
    next_plan VARCHAR(3000) NULL,
    teacher_feedback VARCHAR(1000) NULL,
    feedback_teacher_id BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_development_log_student_day (student_id, log_date),
    KEY idx_development_log_team_date (team_id, log_date),
    KEY idx_development_log_topic_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开发日志表';

-- 演示课题数据。正式项目中请由出题管理模块写入。
INSERT INTO topic (title, description, direction, difficulty, teacher_id, min_members, max_members, status)
SELECT '基于 Java 的综合实训选题系统', '实现出题、组队选题、过程文档与开发日志管理等功能。', 'Java Web', 'A', 2001, 3, 5, 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM topic WHERE title = '基于 Java 的综合实训选题系统');
