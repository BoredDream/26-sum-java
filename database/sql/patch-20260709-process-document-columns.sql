USE training_selection_system;

ALTER TABLE process_document
  ADD COLUMN stage_id BIGINT NULL COMMENT '关联阶段任务ID'
  AFTER teacher_feedback;

ALTER TABLE process_document
  ADD COLUMN remark VARCHAR(500) NULL COMMENT '备注'
  AFTER stage_id;
