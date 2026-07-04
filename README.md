# 基于 Java 的综合实训选题系统

本仓库用于《基于 Java 的综合实训选题系统》的统一开发。所有成员必须基于同一个仓库、同一个后端骨架、同一套数据库 V1 开发。

## 1. 当前目录

```text
26-sum-java/
├── backend/                 # Spring Boot 后端项目骨架
├── database/                # 数据库 SQL 和数据字典
│   ├── sql/
│   │   ├── schema-v1.sql    # 数据库总表结构 V1
│   │   └── seed-v1.sql      # 最小测试数据
│   └── docs/
│       └── data-dictionary.md
└── 开发参考文档/              # 需求、开发规范、GitHub 管理、接口规范
```

## 2. 技术栈

| 类型   | 技术          |
| ---- | ----------- |
| 后端语言 | Java        |
| JDK  | JDK 21      |
| 后端框架 | Spring Boot |
| 数据库  | MySQL       |
| 持久层  | MyBatis     |
| 构建工具 | Maven       |
| 前端   | Vue，后续统一整合  |
| 协作   | GitHub      |

## 3. 当前已完成

- 数据库总表结构 V1：`database/sql/schema-v1.sql`
- 最小测试数据：`database/sql/seed-v1.sql`
- 数据字典：`database/docs/data-dictionary.md`
- 后端 Spring Boot 最小骨架：`backend/`
- 统一返回格式：`Result`
- 统一分页格式：`PageResult`
- 全局异常处理：`GlobalExceptionHandler`
- 登录拦截器预留：`LoginInterceptor`
- 健康检查接口：`GET /api/health`
- 模块包目录：`topic`、`selection`、`attendance`、`info`、`score`

## 4. 后端启动准备

进入后端目录：

```bash
cd backend
```

复制配置模板：

```bash
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
```

修改 `application-local.yml` 中的 MySQL 用户名和密码。

初始化数据库：

```bash
mysql -u root -p < ../database/sql/schema-v1.sql
mysql -u root -p < ../database/sql/seed-v1.sql
```

启动项目：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

健康检查：

```text
GET http://localhost:8080/api/health
```

## 5. 模块开发边界

| 模块          | 包路径                              | 负责人开发范围             |
| ----------- | -------------------------------- | ------------------- |
| 出题管理模块      | `com.training.system.topic`      | 题目、资料、审核、题库管理       |
| 选题管理模块      | `com.training.system.selection`  | 团队、选题、过程文档、开发日志     |
| 学生考勤管理模块    | `com.training.system.attendance` | 签到、补签、考勤统计          |
| 信息管理模块      | `com.training.system.info`       | 账号、学生、教师、公告、日志      |
| 成绩评估与过程分析模块 | `com.training.system.score`      | 成绩、贡献度、AI 报告、Git 分析 |

## 6. 开发前要求

每个模块正式编码前必须提交：

1. 本模块接口清单。
2. 本模块页面清单。
3. 本模块依赖表说明。
4. DTO/VO 草案。
5. 正常、异常、权限、边界测试用例。

数据库结构以 `database/sql/schema-v1.sql` 为准。任何表结构调整必须先通知项目负责人。

## 7. GitHub 协作

推荐分支：

```text
main
 dev
 feature/topic
 feature/selection
 feature/attendance
 feature/info
 feature/score
```

所有功能分支从 `dev` 拉出，开发完成后提交 Pull Request 到 `dev`，由项目负责人或技术负责人审核合并。
