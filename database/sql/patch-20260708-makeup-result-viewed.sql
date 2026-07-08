USE training_selection_system;

ALTER TABLE makeup_sign_apply
  ADD COLUMN result_viewed TINYINT NOT NULL DEFAULT 0 COMMENT '学生是否已查看审核结果：0-未查看，1-已查看'
  AFTER audit_time;

UPDATE makeup_sign_apply SET result_viewed = 1 WHERE audit_status != 0;
