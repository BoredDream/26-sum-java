# 前端实现计划：Vue 3 + TypeScript + Vite 综合实训选题系统

## 项目上下文

仓库 `/home/yaoyao/26-sum-java` 为前后端分离单仓库项目，前端工程位于 `frontend/`。
当前已完成阶段 1-3：
- 工程初始化（package.json、vite.config.ts、tsconfig、Element Plus、Axios、Pinia、Vue Router）
- 登录鉴权与路由守卫（auth store、request 封装、LoginView、路由守卫）
- 布局、角色菜单与错误页（BasicLayout、permission utils、错误页）

剩余需完成阶段 4-10。

## 代码规范与约束

- 所有 API 调用通过 `src/utils/request.ts` 中的 `get/post/put/patch/del/uploadFile/downloadFile`。
- 后端返回统一包装 `Result<T>`，分页返回 `PageResult<T>`，请求函数直接返回 `T`。
- 路由模式 `createWebHistory`，baseURL `/api`，Vite 代理到 `http://localhost:8080`。
- UI 使用 Element Plus 全量自动导入，图标使用 `@element-plus/icons-vue`。
- 状态管理 Pinia，`useAuthStore` 提供 `user/role/isLoggedIn/getHomePath`。
- 角色：STUDENT / TEACHER / ADMIN。
- 类型定义放 `src/types/<module>.ts`，API 封装放 `src/api/<module>.ts`。
- 视图按 `src/views/<module>/<Name>View.vue` 组织。
- 通用组件：`PageHeader.vue`、`StatusTag.vue` 已存在，按需扩展。
- 时间格式化用 `src/utils/format.ts`；文件下载用 `src/utils/download.ts`。
- 所有删除/关闭/审核拒绝/恢复备份/清理日志操作需二次确认。
- 表单提交按钮加 loading。
- 列表页接入 loading、empty、error。

## 后端接口差异（必须遵守）

| 差异点 | 实际后端 | 前端处理 |
|---|---|---|
| 登录路径 | `/api/auth/login` | 使用 `/auth/login` |
| 考勤模块前缀 | 任务 `/attendance/task`、记录 `/attendance/record`、统计 `/attendance/statistics`、补签 `/attendance/makeup` | `api/attendance.ts` 中按实际路径封装 |
| 过程文档上传 | `@RequestParam` + `@RequestPart file`，无 `versionNo` | 表单保留 versionNo 展示，上传时不传 |
| 公告创建/更新 | `@RequestParam` + 文件上传，非 JSON | 提交时构造 FormData |
| 学生/教师改密码 | 需要 `@RequestParam studentId/teacherId` | 从 `auth.userId` 或 `auth.relatedId` 取值 |
| 选题申请状态 | `SelectionVO.status` 为字符串（如 PENDING） | StatusTag.vue 已按字符串映射 |

## 阶段 4：选题主流程（核心验收路径）

**范围**：补全 `src/types/selection.ts`、`src/api/selection.ts` 中已定义的接口对应的视图。

- `TopicBrowseView.vue`：学生浏览可选课题，搜索、查看详情、提交选题申请。
- `MyApplicationView.vue`：学生查看我的选题申请及状态。
- `PendingApplicationsView.vue`：教师审核待处理选题申请。
- `MyDocumentsView.vue`：学生上传/下载过程文档。
- `DocumentsFeedbackView.vue`：教师查看团队文档并给出反馈/退回。
- `MyLogsView.vue`：学生提交开发日志。
- `LogsFeedbackView.vue`：教师查看日志并反馈。
- `TeamDetailView.vue`：共用团队详情（可选，用于教师/管理员查看）。

**必须修复**：`src/views/selection/MyTeamView.vue` 存在大量标签未闭合（缺少 `>`），必须全部修复。

## 阶段 5：出题模块

- 创建 `src/types/topic.ts`、`src/api/topic.ts`。
- 教师：`TopicListView.vue`、`TopicCreateView.vue`、`TopicEditView.vue`、`TopicFilesView.vue`。
- 共用详情：`TopicDetailView.vue`。
- 管理员：`TopicReviewView.vue`（审核）、`TopicManageView.vue`（题库管理，可关闭/开放题目）。

后端 topic 相关接口位于 `backend/src/main/java/com/training/system/topic/`。

## 阶段 6：信息管理模块

- 创建 `src/types/info.ts`、`src/api/info.ts`。
- 仪表盘：`DashboardView.vue`（已存在，可扩展统计卡片）。
- 管理员：`StudentManageView.vue`、`TeacherManageView.vue`、`BackupView.vue`、`LogManageView.vue`。
- 公告：`NoticeView.vue`（管理员管理、所有角色查看）。
- 注意公告表单使用 FormData，导入/导出/下载使用 blob。

后端 info 相关接口位于 `backend/src/main/java/com/training/system/info/`。

## 阶段 7：考勤模块

- 创建 `src/types/attendance.ts`、`src/api/attendance.ts`。
- 教师/管理员：`AttendanceTaskView.vue`、`AttendanceRecordsView.vue`、`MakeupManageView.vue`、`AttendanceStatisticsView.vue`。
- 学生：`AttendanceRecordsView.vue`（我的考勤）、`MakeupManageView.vue`（我的补签）。
- 统计使用 ECharts 饼图或统计卡片。

后端 attendance 相关接口位于 `backend/src/main/java/com/training/system/attendance/`。

## 阶段 8：成绩模块

- 创建 `src/types/score.ts`、`src/api/score.ts`。
- 教师/管理员：`StageTaskView.vue`、`ProgressView.vue`、`ScoreListView.vue`。
- 学生：`MyScoreView.vue`。
- 分数输入限制 0–100，总分以后端返回为准。

后端 score 相关接口位于 `backend/src/main/java/com/training/system/score/`。

## 阶段 9：统一交互组件

- 创建/完善 `src/components/PageHeader.vue`、`SearchBar.vue`、`StatusTag.vue`、`FileUploadButton.vue`、`ConfirmAction.vue`、`ResultTable.vue`。
- `src/utils/download.ts`（blob 下载 + 文件名解析）、`src/utils/format.ts`（日期/文件大小）已存在，按需扩展。
- 所有列表页接入 loading、empty、error。
- 所有删除/关闭/审核拒绝/恢复备份/清理日志操作二次确认。
- 表单提交按钮加 loading。

## 阶段 10：seed 密码修复、构建验证与文档

- 更新 `database/sql/seed-v1.sql`：将占位密码替换为有效 BCrypt 哈希（学生/教师 `123456`，管理员 `admin123`），确保验收账号可登录。
- 创建 `frontend/.gitignore`：排除 `node_modules/`、`dist/`、`.env.local`。
- 更新根目录 `README.md` 与 `frontend/README.md`，补充前端启动、构建命令。
- 运行 `npm run build` 验证无 TypeScript/ESLint 错误。

## 验证方式

1. 开发期验证：每完成一个阶段，`npm run dev` 启动无报错，浏览器访问对应页面无白屏。
2. 构建验证：`cd frontend && npm run build`，期望 `dist/` 生成且无错误。
3. 类型检查：`vue-tsc --noEmit` 无错误。
4. ESLint：`npm run lint` 无错误（或仅有可自动修复的问题）。
