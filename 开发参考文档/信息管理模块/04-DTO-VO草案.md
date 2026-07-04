# 信息管理模块 - DTO/VO 草案

## 一、DTO（Data Transfer Object - 请求参数）

### LoginDTO（登录请求）
```java
- username: String (@NotBlank)  // 用户名（学号/工号）
- password: String (@NotBlank)  // 密码
- role: String (@NotBlank)      // 角色（STUDENT/TEACHER/ADMIN）
```

### NoticeCreateDTO（新增公告）
```java
- title: String (@NotBlank)     // 标题
- content: String (@NotBlank)   // 内容
- type: String (@NotBlank)      // 类型
- topFlag: Integer              // 置顶标记（默认0）
```

### NoticeUpdateDTO（更新公告）
```java
- title: String           // 标题（可选更新）
- content: String         // 内容（可选更新）
- type: String            // 类型（可选更新）
- topFlag: Integer        // 置顶标记（可选更新）
```

### NoticeQueryDTO（公告查询）
```java
- keyword: String         // 搜索关键词
- type: String            // 类型筛选
- pageNum: Integer (默认1)
- pageSize: Integer (默认15)
```

### StudentCreateDTO（新增学生）
```java
- studentNo: String (@NotBlank)   // 学号
- studentName: String (@NotBlank) // 姓名
- major: String (@NotBlank)       // 专业
- className: String (@NotBlank)   // 班级
- phone: String                   // 手机号
- email: String                   // 邮箱
- password: String                // 登录密码（默认123456）
```

### TeacherCreateDTO（新增教师）
```java
- teacherNo: String        // 工号
- teacherName: String      // 姓名
- office: String           // 教研室
- title: String            // 职称
- phone: String            // 手机号
- email: String            // 邮箱
- password: String         // 登录密码（默认123456）
```

## 二、VO（View Object - 响应数据）

### LoginVO（登录响应）
```java
- userId: Long
- username: String
- role: String              // STUDENT/TEACHER/ADMIN
- name: String              // 真实姓名
```

### NoticeVO（公告视图）
```java
- noticeId: Long
- title: String
- content: String
- type: String
- attachPath: String
- topFlag: Integer
- publisherId: Long
- publisherName: String     // 发布人姓名（JOIN teacher）
- createTime: LocalDateTime
- updateTime: LocalDateTime
```

### StudentVO（学生视图）
```java
- studentId: Long
- studentNo: String
- studentName: String
- major: String
- className: String
- phone: String
- email: String
- status: Integer           // 来自 user_account.status
- statusText: String        // "正常"/"禁用"
- createTime: LocalDateTime
```

### TeacherVO（教师视图）
```java
- teacherId: Long
- teacherNo: String
- teacherName: String
- office: String
- title: String
- phone: String
- email: String
- role: String              // 来自 user_account.role
- roleText: String          // "管理员"/"教师"
- createTime: LocalDateTime
```

### BackupVO（备份视图）
```java
- backupId: Long
- backupTime: LocalDateTime
- filePath: String
- fileSize: String
- operatorId: Long
- operatorName: String      // 操作人姓名（JOIN teacher）
```

### OperateLogVO（日志视图）
```java
- logId: Long
- operateUserId: Long
- operateUserName: String   // 操作人姓名（JOIN teacher/student）
- operateType: String
- operateContent: String
- operateTime: LocalDateTime
```
