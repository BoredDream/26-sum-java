# 前端实现细节 Spec

本文档用于指导前端实现 Agent（Kimi）在当前仓库中创建并实现 `frontend/` 工程。实现必须以当前后端代码的真实接口为准，不要直接照搬早期参考文档中的过期接口路径。

## 1. 目标与范围

第一阶段目标：

1. 创建可运行的 Vue 前端工程。
2. 打通登录 session、角色路由、统一请求封装。
3. 完成核心端到端主流程页面：
   `登录 -> 创建团队 -> 入队申请 -> 队长审核 -> 浏览课题 -> 提交选题 -> 教师审核 -> 上传/下载过程文档 -> 教师反馈 -> 提交开发日志 -> 教师反馈日志`。
4. 补齐出题、信息、考勤、成绩模块的基础列表/表单页面，使后端已有接口都能被人工验收。

不在第一阶段实现：

1. AI 报告和 Git 仓库分析。当前后端未提供对应 Controller，不要在前端写假接口。
2. 复杂可视化大屏。成绩、考勤统计只做必要图表或统计卡片。
3. WebSocket、实时协作、离线缓存。

## 2. 技术栈

必须使用：

| 类型 | 选型 |
|---|---|
| 框架 | Vue 3 |
| 语言 | TypeScript |
| 构建工具 | Vite |
| UI 组件 | Element Plus |
| 路由 | Vue Router |
| 状态管理 | Pinia |
| HTTP | Axios |
| 图标 | Element Plus Icons Vue |
| 日期处理 | dayjs |

建议加入：

| 类型 | 选型 |
|---|---|
| 代码规范 | ESLint + Prettier |
| 图表 | ECharts，仅考勤统计或成绩概览需要时使用 |

禁止：

1. 不要引入第二套 UI 组件库。
2. 不要在页面内硬编码完整后端域名。
3. 不要把账号密码、数据库连接、后端密钥写入前端代码。
4. 不要修改 `backend/` 代码来适配前端，除非项目负责人另行确认。

## 3. 工程目录

在仓库根目录创建：

```text
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   ├── topic.ts
│   │   ├── selection.ts
│   │   ├── info.ts
│   │   ├── attendance.ts
│   │   └── score.ts
│   ├── router/
│   │   └── index.ts
│   ├── stores/
│   │   └── auth.ts
│   ├── layouts/
│   │   └── BasicLayout.vue
│   ├── components/
│   │   ├── PageHeader.vue
│   │   ├── SearchBar.vue
│   │   ├── StatusTag.vue
│   │   ├── FileUploadButton.vue
│   │   └── ConfirmAction.vue
│   ├── utils/
│   │   ├── request.ts
│   │   ├── result.ts
│   │   ├── download.ts
│   │   ├── format.ts
│   │   └── permission.ts
│   ├── types/
│   │   ├── api.ts
│   │   ├── auth.ts
│   │   ├── topic.ts
│   │   ├── selection.ts
│   │   ├── attendance.ts
│   │   └── score.ts
│   └── views/
│       ├── login/
│       ├── dashboard/
│       ├── topic/
│       ├── selection/
│       ├── info/
│       ├── attendance/
│       ├── score/
│       └── error/
```

## 4. 开发启动配置

Vite 代理必须这样配置，前端统一请求 `/api`：

```ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

如果后端临时跑在其他端口，只改 `vite.config.ts` 的 proxy target，不改业务代码。

## 5. 统一请求封装

后端统一返回：

```ts
export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}
```

`src/utils/request.ts` 要求：

1. `baseURL: '/api'`
2. `withCredentials: true`，后端使用 session 鉴权。
3. 请求成功但 `code !== 200` 时，用 `ElMessage.error(message)` 提示并 reject。
4. HTTP 401 或业务 code 401 时，清空 auth store 并跳转 `/login`。
5. 下载接口必须支持 `responseType: 'blob'`，不要套普通 JSON 解析。

示例结构：

```ts
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
})
```

## 6. 登录与权限

实际接口：

| 功能 | 方法 | 路径 |
|---|---|---|
| 登录 | POST | `/api/auth/login` |
| 退出 | POST | `/api/auth/logout` |
| 当前用户 | GET | `/api/auth/me` |

登录请求：

```ts
interface LoginDTO {
  username: string
  password: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
}
```

登录响应：

```ts
interface LoginVO {
  userId: number
  relatedId: number
  username: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
  name: string
}
```

前端要求：

1. 登录页路由 `/login`，包含用户名、密码、角色选择。
2. 登录成功后保存 `LoginVO` 到 Pinia，不保存密码。
3. 刷新页面时调用 `/api/auth/me` 恢复登录态。
4. 未登录访问业务页面跳转 `/login`。
5. 已登录访问 `/login` 跳转到对应角色首页。
6. 菜单和按钮按角色隐藏，但不能认为前端隐藏就是权限控制。

角色首页：

| 角色 | 默认跳转 |
|---|---|
| STUDENT | `/selection/my-team` |
| TEACHER | `/topic/my-list` |
| ADMIN | `/admin/dashboard` |

## 7. 布局与交互规范

使用一个后台管理布局 `BasicLayout.vue`：

1. 左侧菜单：按角色显示模块入口。
2. 顶部栏：系统名、当前用户、退出按钮。
3. 主内容区：页面标题、搜索区、操作区、表格/表单。

UI 约束：

1. 使用 Element Plus 的表格、分页、表单、对话框、抽屉、上传组件。
2. 所有删除、关闭、审核拒绝、恢复备份等风险操作必须二次确认。
3. 列表页统一提供加载态、空状态、错误提示。
4. 表单提交按钮必须有 loading，防止重复提交。
5. 文件上传必须限制格式并展示后端错误消息。

## 8. 路由规划

公共路由：

| 路径 | 页面 |
|---|---|
| `/login` | 登录 |
| `/403` | 无权限 |
| `/404` | 不存在 |
| `/500` | 系统异常 |

管理端：

| 路径 | 页面 |
|---|---|
| `/admin/dashboard` | 仪表盘 |
| `/admin/students` | 学生管理 |
| `/admin/teachers` | 教师管理 |
| `/admin/notices` | 公告管理 |
| `/admin/backups` | 数据备份 |
| `/admin/logs` | 操作日志 |
| `/topic/review` | 题目审核 |
| `/topic/manage` | 题库管理 |

教师端：

| 路径 | 页面 |
|---|---|
| `/topic/my-list` | 我的题目 |
| `/topic/create` | 新增题目 |
| `/topic/edit/:topicId` | 修改题目 |
| `/topic/:topicId/files` | 题目资料 |
| `/selection/applications/pending` | 选题审核 |
| `/selection/documents` | 过程文档反馈 |
| `/selection/logs` | 开发日志反馈 |
| `/attendance/tasks` | 签到任务 |
| `/attendance/records` | 考勤记录 |
| `/attendance/makeup` | 补签审核 |
| `/attendance/statistics` | 考勤统计 |
| `/score/stage-tasks` | 阶段任务 |
| `/score/progress` | 过程评价 |
| `/score/list` | 成绩管理 |

学生端：

| 路径 | 页面 |
|---|---|
| `/topic/browse` | 题目浏览 |
| `/selection/my-team` | 我的团队 |
| `/selection/topics` | 可选课题 |
| `/selection/my-application` | 我的选题申请 |
| `/selection/documents/my` | 我的过程文档 |
| `/selection/logs/my` | 我的开发日志 |
| `/attendance/tasks` | 签到任务 |
| `/attendance/records` | 我的考勤 |
| `/attendance/makeup` | 我的补签 |
| `/score/my` | 我的成绩 |

共用详情页：

| 路径 | 页面 |
|---|---|
| `/topic/detail/:topicId` | 题目详情 |
| `/selection/teams/:teamId` | 团队详情 |

## 9. 接口封装清单

### 9.1 出题模块

前缀：`/api/topic`

| 函数 | 方法与路径 | 说明 |
|---|---|---|
| `createTopic(data)` | POST `/topic` | 新增题目 |
| `updateTopic(topicId, data)` | PUT `/topic/{topicId}` | 修改题目 |
| `deleteTopic(topicId)` | DELETE `/topic/{topicId}` | 删除题目 |
| `getTopic(topicId)` | GET `/topic/{topicId}` | 详情 |
| `pageTopics(params)` | GET `/topic/page` | 分页 |
| `submitTopic(topicId)` | POST `/topic/{topicId}/submit` | 提交审核 |
| `reviewTopic(topicId, data)` | POST `/topic/{topicId}/review` | 管理员审核 |
| `openTopic(topicId)` | POST `/topic/{topicId}/open` | 开放题目 |
| `closeTopic(topicId)` | POST `/topic/{topicId}/close` | 关闭题目 |
| `uploadTopicFile(topicId, formData)` | POST `/topic/{topicId}/file` | 上传资料 |
| `deleteTopicFile(fileId)` | DELETE `/topic/file/{fileId}` | 删除资料 |
| `listTopicFiles(topicId)` | GET `/topic/{topicId}/file` | 资料列表 |
| `downloadTopicFile(fileId)` | GET `/topic/file/{fileId}/download` | 下载资料 |
| `listTopicReviews(topicId)` | GET `/topic/{topicId}/review` | 审核记录 |

`TopicCreateDTO` / `TopicUpdateDTO` 字段：

```ts
interface TopicForm {
  topicName: string
  topicType: string
  difficulty: string
  studentLimit: number
  teamLimit?: number
  topicContent: string
  developTools?: string
  technicalRoute: string
  selectionStartTime?: string
  selectionEndTime?: string
  status?: number
  modifyReason?: string
}
```

状态约定：

| 字段 | 值 |
|---|---|
| `status` | 0 草稿，1 待审核，2 审核通过，3 退回修改，4 不予通过 |
| `openStatus` | 0 未开放，1 已开放，2 已关闭 |

页面要求：

1. 教师“我的题目”默认传 `teacherId = auth.relatedId`。
2. 学生题目浏览只展示审核通过且已开放的题目，查询时传 `status=2&openStatus=1`。
3. 管理员题目审核页默认查 `status=1`。
4. 上传资料使用 multipart，字段至少包含 `file`，如后端需要说明字段再补 `fileDesc`。

### 9.2 选题模块

实际接口全部在 `/api/selection` 下。

| 函数 | 方法与路径 | 角色 |
|---|---|---|
| `createTeam(data)` | POST `/selection/teams` | STUDENT |
| `getMyTeam()` | GET `/selection/teams/my` | STUDENT |
| `getTeam(teamId)` | GET `/selection/teams/{teamId}` | STUDENT/TEACHER |
| `joinTeam(teamId, data)` | POST `/selection/teams/{teamId}/join-requests` | STUDENT |
| `listJoinRequests(teamId)` | GET `/selection/teams/{teamId}/join-requests` | 队长 |
| `auditJoinRequest(requestId, data)` | PATCH `/selection/teams/join-requests/{requestId}/audit` | 队长 |
| `updateMemberWork(teamId, studentId, data)` | PUT `/selection/teams/{teamId}/members/{studentId}/work-content` | 队长 |
| `listSelectableTopics(keyword?)` | GET `/selection/topics` | STUDENT |
| `getSelectableTopic(topicId)` | GET `/selection/topics/{topicId}` | STUDENT |
| `submitSelection(data)` | POST `/selection/applications` | 队长 |
| `getMySelection()` | GET `/selection/applications/my` | STUDENT |
| `listPendingSelections()` | GET `/selection/applications/pending` | TEACHER |
| `auditSelection(selectionId, data)` | PATCH `/selection/applications/{selectionId}/audit` | TEACHER |
| `uploadDocument(formData)` | POST `/selection/documents` | STUDENT |
| `listDocuments()` | GET `/selection/documents` | STUDENT/TEACHER |
| `feedbackDocument(documentId, data)` | PATCH `/selection/documents/{documentId}/feedback` | TEACHER |
| `downloadDocument(documentId)` | GET `/selection/documents/{documentId}/download` | STUDENT/TEACHER |
| `createLog(data)` | POST `/selection/logs` | STUDENT |
| `listLogs()` | GET `/selection/logs` | STUDENT/TEACHER |
| `feedbackLog(logId, data)` | PATCH `/selection/logs/{logId}/feedback` | TEACHER |

主要 DTO：

```ts
interface CreateTeamDTO {
  teamName: string
  introduction?: string
  maxSize?: number
}

interface JoinTeamDTO {
  applyMessage?: string
}

interface AuditDTO {
  approved: boolean
  opinion?: string
}

interface SubmitSelectionDTO {
  topicId: number
  selectionReason: string
}

interface UploadDocumentForm {
  documentName: string
  documentType: string
  projectStage: string
  versionNo: string
  file: File
}

interface DocumentFeedbackDTO {
  feedback: string
  returned: boolean
}

interface CreateDevelopmentLogDTO {
  title: string
  logDate: string
  workContent: string
  completionStatus: string
  problemDescription?: string
  nextPlan?: string
}

interface LogFeedbackDTO {
  feedback: string
}
```

过程文档上传限制：

1. 允许：`doc`、`docx`、`pdf`、`xls`、`xlsx`、`zip`、`rar`。
2. 不要允许 `.txt`，后端会拒绝。
3. 使用 `multipart/form-data`。

主流程页面要求：

1. `/selection/my-team`
   - 无团队：显示创建团队表单。
   - 有团队：显示团队信息、成员列表、入队申请入口。
   - 当前用户是队长：显示入队申请审核、成员分工编辑。
2. `/selection/topics`
   - 展示可选课题列表。
   - 队长可提交选题申请，普通成员按钮禁用并说明“仅队长可提交”。
3. `/selection/applications/pending`
   - 教师查看待审核申请，支持通过/驳回弹窗。
4. `/selection/documents/my`
   - 学生上传、查看、下载过程文档。
5. `/selection/documents`
   - 教师查看自己指导团队文档，反馈通过或退回。
6. `/selection/logs/my`
   - 学生新增和查看开发日志。
7. `/selection/logs`
   - 教师查看日志并反馈。

### 9.3 信息管理模块

实际前缀：

| 功能 | 前缀 |
|---|---|
| 学生 | `/api/students` |
| 教师 | `/api/teachers` |
| 公告 | `/api/notices` |
| 备份 | `/api/backups` |
| 操作日志 | `/api/logs` |
| 仪表盘 | `/api/dashboard` |

主要接口：

| 模块 | 方法与路径 |
|---|---|
| Dashboard | GET `/dashboard` |
| Student | POST `/students`，PUT `/students/{studentId}`，GET `/students/{studentId}`，GET `/students/page`，POST `/students/{studentId}/reset-password`，POST `/students/{studentId}/toggle-status`，DELETE `/students/{studentId}`，GET `/students/export`，POST `/students/import`，POST `/students/change-password` |
| Teacher | POST `/teachers`，PUT `/teachers/{teacherId}`，GET `/teachers/{teacherId}`，GET `/teachers/page`，POST `/teachers/{teacherId}/reset-password`，POST `/teachers/{teacherId}/toggle-role`，DELETE `/teachers/{teacherId}`，GET `/teachers/export`，POST `/teachers/change-password` |
| Notice | POST `/notices`，PUT `/notices/{noticeId}`，DELETE `/notices/{noticeId}`，GET `/notices/{noticeId}`，GET `/notices/page`，POST `/notices/{noticeId}/top`，GET `/notices/{noticeId}/download` |
| Backup | POST `/backups`，GET `/backups/page`，GET `/backups/{backupId}/download`，POST `/backups/{backupId}/restore`，DELETE `/backups/{backupId}`，POST `/backups/cleanup` |
| Logs | GET `/logs/page`，POST `/logs/clear` |

页面要求：

1. 管理员可进入学生、教师、公告、备份、日志页面。
2. 教师和学生只需要能查看公告、个人信息、修改密码。
3. 导入、导出、下载都按 blob 或 multipart 处理。
4. 恢复备份、清理日志必须二次确认。

### 9.4 考勤模块

前缀：`/api/attendance`

| 函数 | 方法与路径 |
|---|---|
| `createTask(data)` | POST `/attendance/task` |
| `finishTask(taskId)` | POST `/attendance/task/{taskId}/finish` |
| `pageTasks(params)` | GET `/attendance/task/page` |
| `getTask(taskId)` | GET `/attendance/task/{taskId}` |
| `sign(data)` | POST `/attendance/record/sign` |
| `pageRecords(params)` | GET `/attendance/record/page` |
| `exportRecords(params)` | GET `/attendance/export` 或 `/attendance/record/export` |
| `createMakeup(data)` | POST `/attendance/makeup` |
| `reviewMakeup(applyId, data)` | POST `/attendance/makeup/{applyId}/review` |
| `pageMakeup(params)` | GET `/attendance/makeup/page` |
| `uploadMakeupProof(formData)` | POST `/attendance/makeup/upload` |
| `statistics(params)` | GET `/attendance/statistics` |

主要 DTO：

```ts
interface AttendanceTaskCreateDTO {
  taskTitle: string
  taskType: number
  scopeType: number
  scopeValue?: string
  startTime: string
  endTime: string
  description?: string
}

interface AttendanceSignDTO {
  taskId: number
  remark?: string
}

interface MakeupApplyDTO {
  taskId: number
  recordId?: number
  applyReason: string
  proofFilePath?: string
}

interface MakeupReviewDTO {
  auditStatus: number
  auditComment?: string
}
```

状态展示：

| 字段 | 值 |
|---|---|
| `taskType` | 1 日常签到，2 阶段签到 |
| `scopeType` | 1 班级，2 团队，3 全部 |
| `task.status` | 0 未开始，1 进行中，2 已结束 |
| `signStatus` | 0 缺勤，1 正常，2 迟到，3 补签 |
| `auditStatus` | 0 待审核，1 通过，2 驳回 |

页面要求：

1. 教师可以发布、结束、查看签到任务。
2. 学生可以查看可签到任务并签到。
3. 学生可以提交补签申请，教师审核。
4. 考勤统计显示总次数、正常、迟到、缺勤、补签、出勤率。

### 9.5 成绩模块

前缀：`/api/score`

| 函数 | 方法与路径 |
|---|---|
| `createStageTask(data)` | POST `/score/stage-task` |
| `pageStageTasks(params)` | GET `/score/stage-task/page` |
| `evaluateStage(data)` | POST `/score/stage-evaluation` |
| `getProgress(params)` | GET `/score/progress` |
| `saveContribution(data)` | POST `/score/contribution` |
| `saveScore(data)` | POST `/score` |
| `pageScores(params)` | GET `/score/page` |
| `getStudentScore(studentId)` | GET `/score/student/{studentId}` |
| `confirmScore(scoreId)` | POST `/score/{scoreId}/confirm` |
| `exportScores(params)` | GET `/score/export` |

主要 DTO：

```ts
interface StageTaskCreateDTO {
  stageName: string
  stageDesc: string
  startTime: string
  endTime: string
  deliverables: string
  scoringCriteria: string
  weight: number
  status?: number
}

interface StageEvaluationDTO {
  stageId: number
  teamId: number
  relatedDocumentId?: number
  docScore: number
  completionScore: number
  innovationScore?: number
  techScore?: number
  comment?: string
  result: number
  isLate?: number
  lateDays?: number
}

interface ScoreSaveDTO {
  teamId: number
  aiReportId?: number
  docScore: number
  attendanceScore: number
  systemScore: number
  defenseScore: number
  teacherComment?: string
  studentScores?: Array<{
    studentId: number
    contributionFactor?: number
    teacherComment?: string
  }>
}
```

页面要求：

1. 教师可发布阶段任务、录入阶段评价、录入贡献度、保存团队成绩、确认成绩。
2. 学生只看个人成绩。
3. 分数输入使用数字输入框，限制 0 到 100。
4. 总分以后端返回为准，前端可预览但不覆盖后端计算结果。

## 10. 菜单权限矩阵

| 菜单 | STUDENT | TEACHER | ADMIN |
|---|---:|---:|---:|
| 题目浏览 | 是 | 是 | 是 |
| 我的题目/新增题目 | 否 | 是 | 否 |
| 题目审核/题库管理 | 否 | 否 | 是 |
| 我的团队/选题申请 | 是 | 否 | 否 |
| 选题审核 | 否 | 是 | 否 |
| 过程文档/开发日志提交 | 是 | 否 | 否 |
| 过程文档/开发日志反馈 | 否 | 是 | 否 |
| 学生/教师/备份/日志管理 | 否 | 否 | 是 |
| 公告查看 | 是 | 是 | 是 |
| 公告管理 | 否 | 可选 | 是 |
| 考勤签到/补签 | 是 | 否 | 否 |
| 考勤任务/审核/统计 | 否 | 是 | 是 |
| 我的成绩 | 是 | 否 | 否 |
| 成绩管理 | 否 | 是 | 是 |

## 11. 第一阶段验收脚本

Kimi 实现后，必须保证人工可以按下面步骤验收：

1. 启动后端：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

2. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

3. 浏览器打开 `http://localhost:5173`。

4. 使用已初始化数据库账号完成：
   - 学生 A 登录，创建团队。
   - 学生 B 登录，申请加入团队。
   - 学生 A 登录，审核入队申请。
   - 学生 A 浏览课题并提交选题申请。
   - 教师登录，审核选题通过。
   - 学生 A 上传 `pdf/docx/zip` 格式过程文档。
   - 学生 A 下载刚上传的过程文档。
   - 教师反馈过程文档。
   - 学生 A 提交开发日志。
   - 教师反馈开发日志。

5. 构建必须通过：

```bash
cd frontend
npm run build
```

## 12. 实现顺序

按以下顺序提交，不要一次性塞完所有页面：

1. `frontend` 工程初始化、Vite 配置、Element Plus、Pinia、Router、Axios。
2. 登录、退出、`/api/auth/me` 恢复登录态、角色路由守卫。
3. `BasicLayout`、角色菜单、错误页。
4. 选题主流程页面和接口封装。
5. 出题模块页面。
6. 信息管理模块页面。
7. 考勤模块页面。
8. 成绩模块页面。
9. 统一空状态、错误状态、loading、二次确认、下载文件名处理。
10. `npm run build` 验证和 README 前端启动说明。

## 13. 提交要求

1. 从 `dev` 拉出前端分支，例如 `feature/frontend-vue`。
2. 提交信息使用 `前端: ...` 格式。
3. 不提交 `node_modules/`、`dist/`、`.env.local`。
4. 若新增 `.env.example`，只能写示例值。
5. 完成后发 PR 到 `dev`，PR 描述必须包含：
   - 已实现页面清单。
   - 已对接接口清单。
   - 本地启动命令。
   - `npm run build` 结果。
   - 尚未实现或受后端限制的功能。

