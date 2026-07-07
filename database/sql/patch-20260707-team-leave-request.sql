USE training_selection_system;

-- 团队离队申请表
CREATE TABLE IF NOT EXISTS team_leave_request (
  request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '离队申请编号',
  team_id BIGINT NOT NULL COMMENT '要离开的团队编号',
  applicant_id BIGINT NOT NULL COMMENT '申请学生编号',
  leave_message VARCHAR(500) NULL COMMENT '离队原因说明',
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-驳回',
  reviewer_id BIGINT NULL COMMENT '审核人学生编号（团队负责人）',
  review_opinion VARCHAR(500) NULL COMMENT '审核意见',
  apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  review_time DATETIME NULL COMMENT '审核时间',
  CONSTRAINT fk_leave_request_team FOREIGN KEY (team_id) REFERENCES team_info(team_id),
  CONSTRAINT fk_leave_request_applicant FOREIGN KEY (applicant_id) REFERENCES student(student_id),
  CONSTRAINT fk_leave_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES student(student_id),
  INDEX idx_leave_request_team_status (team_id, audit_status),
  INDEX idx_leave_request_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队离队申请表';
