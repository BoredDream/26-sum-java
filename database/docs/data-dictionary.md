# 数据库设计 V1 数据字典

本文档对应 `database/sql/schema-v1.sql`，作为项目正式分模块开发前的数据库总表结构初稿。各模块不得私自新增核心公共表；如需调整字段，先提交给项目负责人确认。

## 1. 公共规则

| 项目 | 约定 |
|---|---|
| 数据库 | `training_selection_system` |
| 字符集 | `utf8mb4` |
| 存储引擎 | InnoDB |
| 主键类型 | `bigint` |
| 主键命名 | `xxx_id` |
| 时间字段 | `create_time`、`update_time`，逻辑删除表可加 `delete_time` |
| 逻辑删除 | 核心业务表使用 `is_deleted`，0-否，1-是 |
| 登录密码 | 只允许存储在 `user_account.password` |

## 2. 表归属

| 模块 | 表 |
|---|---|
| 公共账号与基础信息 | `user_account`、`student`、`teacher` |
| 出题管理模块 | `project_topic`、`topic_file`、`topic_review` |
| 选题管理模块 | `team_info`、`team_member`、`topic_selection`、`process_document`、`development_log` |
| 学生考勤管理模块 | `attendance_task`、`attendance_record`、`makeup_sign_apply` |
| 信息管理模块 | `notice`、`data_backup`、`operate_log` |
| 成绩评估与过程分析模块 | `stage_task`、`stage_evaluation`、`contribution`、`score`、`student_score`、`score_detail`、`ai_report` |
| Git 过程分析 | `code_repository`、`code_commit` |

## 3. 关键边界

| 规则 | 说明 |
|---|---|
| 过程文档 | 统一存入 `process_document`，归选题管理模块 |
| 开发日志 | 统一存入 `development_log`，归选题管理模块 |
| 考勤模块 | 只处理签到、补签、统计，不做文档提交和文档审核 |
| 成绩模块 | 读取过程文档、开发日志、考勤、Git 提交等数据，不修改原始过程数据 |
| Git 数据 | 使用 `code_repository` 和 `code_commit`，不使用文件上传表记录代码过程 |
| 题目容量 | `student_limit` 表示单个团队人数上限；`team_limit` 表示题目最多可被多少团队选择 |

## 4. 状态字典

### 4.1 用户角色

| 值 | 说明 |
|---|---|
| STUDENT | 学生 |
| TEACHER | 教师 |
| ADMIN | 管理员 |

### 4.2 通用账号状态

| 值 | 说明 |
|---|---|
| 0 | 禁用 |
| 1 | 正常 |

### 4.3 题目审核状态 `project_topic.status`

| 值 | 说明 |
|---|---|
| 0 | 草稿 |
| 1 | 待审核 |
| 2 | 审核通过 |
| 3 | 退回修改 |
| 4 | 不予通过 |

### 4.4 题目开放状态 `project_topic.open_status`

| 值 | 说明 |
|---|---|
| 0 | 未开放 |
| 1 | 已开放 |
| 2 | 已关闭 |

### 4.5 选题申请状态 `topic_selection.audit_status`

| 值 | 说明 |
|---|---|
| 0 | 待审核 |
| 1 | 通过 |
| 2 | 驳回 |
| 3 | 已撤回 |

### 4.6 签到状态 `attendance_record.sign_status`

| 值 | 说明 |
|---|---|
| 0 | 缺勤 |
| 1 | 正常 |
| 2 | 迟到 |
| 3 | 补签 |

### 4.7 补签审核状态 `makeup_sign_apply.audit_status`

| 值 | 说明 |
|---|---|
| 0 | 待审核 |
| 1 | 通过 |
| 2 | 驳回 |

### 4.8 成绩状态 `score.status`

| 值 | 说明 |
|---|---|
| 0 | 草稿 |
| 1 | 已确认 |
| 2 | 已锁定 |

## 5. 关键外键关系

| 表 | 字段 | 关联表 |
|---|---|---|
| `project_topic` | `teacher_id` | `teacher.teacher_id` |
| `topic_file` | `topic_id` | `project_topic.topic_id` |
| `team_info` | `leader_id` | `student.student_id` |
| `team_member` | `team_id`、`student_id` | `team_info`、`student` |
| `topic_selection` | `team_id`、`topic_id` | `team_info`、`project_topic` |
| `process_document` | `team_id`、`topic_id`、`uploader_id` | `team_info`、`project_topic`、`student` |
| `attendance_record` | `task_id`、`student_id` | `attendance_task`、`student` |
| `makeup_sign_apply` | `task_id`、`record_id`、`student_id` | `attendance_task`、`attendance_record`、`student` |
| `score` | `team_id`、`teacher_id`、`ai_report_id` | `team_info`、`teacher`、`ai_report` |
| `student_score` | `score_id`、`student_id` | `score`、`student` |
| `code_repository` | `team_id` | `team_info` |
| `code_commit` | `repo_id`、`team_id`、`student_id` | `code_repository`、`team_info`、`student` |

## 6. 开发前确认事项

1. 是否需要把班级拆成单独的 `class_info` 表。
2. `team_limit` 是否必须启用；如不启用，题目是否永远不显示满员。
3. 补签申请时限，例如签到结束后 24 小时内。
4. 个人成绩计算公式和贡献度系数范围。
5. Git 作者邮箱与学生身份的匹配方式。
