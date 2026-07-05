# 综合实训选题管理模块（按 Controller / DTO / Entity / Mapper / Service / VO 分层）

这份代码按你截图中的包结构编写，根目录与包名均已对齐：

```text
backend/src/main/java/com/training/system/selection
├── controller   # 对外 REST 接口、统一异常处理
├── dto          # 前端请求参数与校验规则
├── entity       # 数据库实体对象
├── mapper       # MyBatis 数据访问接口与 SQL
├── service      # 业务逻辑、权限校验、事务控制
└── vo           # 返回给前端的视图对象
```

模块覆盖功能：

- 学生创建团队、查看团队、申请加入团队；
- 团队负责人审核入队申请、维护成员分工；
- 学生查询课题并提交选题申请；
- 教师审核本人课题的选题申请；
- 学生上传过程文档，教师查看并反馈；
- 学生提交开发日志，教师查看并反馈。

## 1. 如何合并到你们现有项目

1. 将 `backend/src/main/java/com/training/system/selection` 文件夹复制到你们仓库对应位置：
   `backend/src/main/java/com/training/system/selection`。
2. 执行 `sql/selection_module.sql` 中的建表脚本。若“出题管理模块”已经有 `topic` 表，请不要重复建表；只需确认其字段名与 `TopicMapper.java` 中的 SQL 对应。
3. 在现有启动类上添加（如果已经有全局 Mapper 扫描，可跳过）：

   ```java
   @MapperScan("com.training.system.selection.mapper")
   ```

4. 确认后端已有以下依赖：Spring Web、Validation、MyBatis、MySQL 驱动。独立示例所需依赖已在 `backend/pom.xml` 中写好。
5. 修改 `backend/src/main/resources/application.yml` 里的 MySQL 地址、数据库账号和密码。

> 本模块使用项目统一的 Session 登录态，从 `HttpSession` 读取 `role` 和 `relatedId` 作为当前角色与学生/教师业务编号。

角色取值：`STUDENT`、`TEACHER`、`ADMIN`。

## 2. 接口一览

| 功能 | 方法 | 地址 |
|---|---|---|
| 创建团队 | POST | `/api/selection/teams` |
| 查看我的团队 | GET | `/api/selection/teams/my` |
| 申请加入团队 | POST | `/api/selection/teams/{teamId}/join-requests` |
| 审核入队申请 | PATCH | `/api/selection/teams/join-requests/{requestId}/audit` |
| 修改成员分工 | PUT | `/api/selection/teams/{teamId}/members/{studentId}/work-content` |
| 查询可选课题 | GET | `/api/selection/topics?keyword=` |
| 查询课题详情 | GET | `/api/selection/topics/{topicId}` |
| 提交选题申请 | POST | `/api/selection/applications` |
| 查询本团队选题申请 | GET | `/api/selection/applications/my` |
| 教师查看待审核申请 | GET | `/api/selection/applications/pending` |
| 教师审核选题申请 | PATCH | `/api/selection/applications/{selectionId}/audit` |
| 上传过程文档 | POST | `/api/selection/documents` |
| 查询文档 | GET | `/api/selection/documents` |
| 教师反馈文档 | PATCH | `/api/selection/documents/{documentId}/feedback` |
| 下载过程文档 | GET | `/api/selection/documents/{documentId}/download` |
| 提交开发日志 | POST | `/api/selection/logs` |
| 查询开发日志 | GET | `/api/selection/logs` |
| 教师反馈开发日志 | PATCH | `/api/selection/logs/{logId}/feedback` |

## 3. 请求示例

### 3.1 学生创建团队

请求头：

```text
Cookie: JSESSIONID=...
Content-Type: application/json
```

请求体：

```json
{
  "teamName": "星火开发小组",
  "introduction": "负责 Java 综合实训项目开发",
  "maxSize": 5
}
```

### 3.2 团队负责人提交选题申请

请求头：

```text
Cookie: JSESSIONID=...
Content-Type: application/json
```

请求体：

```json
{
  "topicId": 1,
  "selectionReason": "团队成员具备 Java Web 和 MySQL 基础，希望完成完整的实训管理闭环。"
}
```

### 3.3 教师审核选题

请求头：

```text
Cookie: JSESSIONID=...
Content-Type: application/json
```

请求体：

```json
{
  "approved": true,
  "opinion": "团队人数和分工合理，同意选题。"
}
```

## 4. 关键业务规则

- 同一学生只能加入一个有效团队；
- 只有团队负责人能够提交选题申请、审核入队申请和调整成员分工；
- 同一团队只能保留一条待审核选题申请；
- 教师只能审核本人发布课题的申请；
- 课题审核通过后，课题状态会更新为 `SELECTED`，团队状态会更新为 `SELECTED`；
- 只有选题审核通过的团队可以上传过程文档、提交开发日志；
- 学生只能查看本团队的过程材料，教师只能查看本人指导课题对应团队的材料；
- 每位学生每天只能提交一条开发日志。

## 5. 运行说明

独立运行时，进入 `backend` 目录后执行：

```bash
mvn spring-boot:run
```

注意：本压缩包没有内置 Maven Wrapper；请使用本机 Maven。若直接合并进你们已有的 `backend` 工程，则无需使用本模块附带的 `pom.xml` 和启动类，只需复制 `selection` 包、SQL 和必要依赖配置。
