# 后端项目骨架

技术栈：Java 18、Spring Boot、MyBatis、MySQL、Maven。

## 本地启动

1. 创建数据库并执行：

```bash
mysql -u root -p < ../database/sql/schema-v1.sql
mysql -u root -p < ../database/sql/seed-v1.sql
```

2. 复制配置模板：

```bash
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
```

3. 修改 `application-local.yml` 中的数据库账号密码。

4. 启动后端：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

健康检查：

```text
GET /api/health
```

## 模块目录

| 模块 | 包 |
|---|---|
| 出题管理 | `com.training.system.topic` |
| 选题管理 | `com.training.system.selection` |
| 学生考勤管理 | `com.training.system.attendance` |
| 信息管理 | `com.training.system.info` |
| 成绩评估与过程分析 | `com.training.system.score` |

各模块负责人只在自己模块包下新增 Controller、DTO、VO、Service、Mapper、Entity。
