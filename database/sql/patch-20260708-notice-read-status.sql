USE training_selection_system;

CREATE TABLE IF NOT EXISTS notice_read_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录编号',
  notice_id BIGINT NOT NULL COMMENT '公告编号',
  user_id BIGINT NOT NULL COMMENT '用户账号编号',
  read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  CONSTRAINT fk_nrr_notice FOREIGN KEY (notice_id) REFERENCES notice(notice_id),
  CONSTRAINT fk_nrr_user FOREIGN KEY (user_id) REFERENCES user_account(user_id),
  UNIQUE KEY uk_notice_user (notice_id, user_id),
  INDEX idx_nrr_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';
