# 出题管理模块 DTO / VO 草案

## 1. 模块名称

出题管理模块

## 2. DTO（请求参数对象）

### 2.1 TopicCreateDTO — 新增题目

```java
package com.training.system.topic.dto;

import jakarta.validation.constraints.*;

public class TopicCreateDTO {
    @NotBlank(message = "项目名称不能为空")
    private String topicName;           // 项目题目名称

    @NotBlank(message = "项目类型不能为空")
    private String topicType;           // 项目类型

    @NotBlank(message = "项目难度不能为空")
    private String difficulty;          // 项目难度

    @NotNull(message = "请填写单个团队人数上限")
    @Min(value = 1, message = "学生人数必须大于0")
    private Integer studentLimit;       // 单个团队人数上限

    private Integer teamLimit;          // 最大团队数，可为空

    @NotBlank(message = "项目内容及要求不能为空")
    private String topicContent;        // 项目内容及要求

    private String developTools;        // 开发工具与软件

    @NotBlank(message = "技术路线不能为空")
    private String technicalRoute;      // 技术路线

    private LocalDateTime selectionStartTime;  // 选题开始时间
    private LocalDateTime selectionEndTime;    // 选题截止时间

    @NotNull(message = "请指定操作类型")
    private Integer status;             // 0-保存草稿, 1-提交审核
}
```

### 2.2 TopicUpdateDTO — 修改题目

```java
package com.training.system.topic.dto;

import jakarta.validation.constraints.*;

public class TopicUpdateDTO {
    @NotBlank(message = "项目名称不能为空")
    private String topicName;

    @NotBlank(message = "项目类型不能为空")
    private String topicType;

    @NotBlank(message = "项目难度不能为空")
    private String difficulty;

    @NotNull(message = "请填写单个团队人数上限")
    @Min(value = 1, message = "学生人数必须大于0")
    private Integer studentLimit;

    private Integer teamLimit;

    @NotBlank(message = "项目内容及要求不能为空")
    private String topicContent;

    private String developTools;

    @NotBlank(message = "技术路线不能为空")
    private String technicalRoute;

    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;

    private String modifyReason;        // 修改原因（特殊情况下修改时填写）
}
```

### 2.3 TopicQueryDTO — 查询题目

```java
package com.training.system.topic.dto;

public class TopicQueryDTO {
    private Integer pageNum = 1;        // 页码，默认1
    private Integer pageSize = 10;      // 每页条数，默认10
    private String keyword;             // 关键词（按题目名称模糊查询）
    private String topicType;           // 项目类型筛选
    private String difficulty;          // 项目难度筛选
    private Integer status;             // 审核状态筛选
    private Integer openStatus;         // 开放状态筛选
    private Long teacherId;             // 指导教师筛选
    private LocalDateTime startTime;    // 创建时间范围-起始
    private LocalDateTime endTime;      // 创建时间范围-截止
}
```

### 2.4 TopicReviewDTO — 审核题目

```java
package com.training.system.topic.dto;

import jakarta.validation.constraints.*;

public class TopicReviewDTO {
    @NotNull(message = "请选择审核结果")
    private Integer reviewResult;       // 审核结果：1-通过, 2-退回修改, 3-不予通过

    @NotBlank(message = "请填写审核意见")
    private String reviewComment;       // 审核意见
}
```

## 3. VO（返回视图对象）

### 3.1 TopicListVO — 题目列表项

```java
package com.training.system.topic.vo;

import java.time.LocalDateTime;

public class TopicListVO {
    private Long topicId;               // 题目编号
    private String topicName;           // 题目名称
    private String topicType;           // 项目类型
    private String difficulty;          // 项目难度
    private String teacherName;         // 指导教师姓名
    private Long teacherId;             // 指导教师编号
    private Integer studentLimit;       // 团队人数上限
    private Integer teamLimit;          // 最大团队数
    private Integer status;             // 审核状态
    private String statusText;          // 审核状态文本
    private Integer openStatus;         // 开放状态
    private String openStatusText;      // 开放状态文本
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

### 3.2 TopicDetailVO — 题目详情

```java
package com.training.system.topic.vo;

import java.time.LocalDateTime;
import java.util.List;

public class TopicDetailVO {
    private Long topicId;               // 题目编号
    private String topicName;           // 题目名称
    private String topicType;           // 项目类型
    private String difficulty;          // 项目难度
    private Long teacherId;             // 指导教师编号
    private String teacherName;         // 指导教师姓名
    private String teacherNo;           // 指导教师工号
    private Integer studentLimit;       // 团队人数上限
    private Integer teamLimit;          // 最大团队数
    private LocalDateTime selectionStartTime;  // 选题开始时间
    private LocalDateTime selectionEndTime;    // 选题截止时间
    private String topicContent;        // 项目内容及要求
    private String developTools;        // 开发工具与软件
    private String technicalRoute;      // 技术路线
    private Integer status;             // 审核状态
    private String statusText;          // 审核状态文本
    private Integer openStatus;         // 开放状态
    private String openStatusText;      // 开放状态文本
    private List<TopicFileVO> files;    // 题目资料文件列表
    private List<TopicReviewVO> reviews;// 审核记录列表
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

### 3.3 TopicFileVO — 资料文件

```java
package com.training.system.topic.vo;

import java.time.LocalDateTime;

public class TopicFileVO {
    private Long fileId;                // 文件编号
    private Long topicId;               // 所属题目编号
    private String fileName;            // 文件名称
    private String fileType;            // 文件类型
    private Long fileSize;              // 文件大小（字节）
    private String fileSizeText;        // 文件大小（可读格式）
    private String fileDesc;            // 文件说明
    private String uploadUserName;      // 上传人姓名
    private LocalDateTime uploadTime;   // 上传时间
}
```

### 3.4 TopicReviewVO — 审核记录

```java
package com.training.system.topic.vo;

import java.time.LocalDateTime;

public class TopicReviewVO {
    private Long reviewId;              // 审核记录编号
    private Long topicId;               // 题目编号
    private String adminName;           // 审核人姓名
    private Integer reviewResult;       // 审核结果
    private String reviewResultText;    // 审核结果文本
    private String reviewComment;       // 审核意见
    private LocalDateTime reviewTime;   // 审核时间
}
```

## 4. Entity（数据库实体，对应三张表）

### 4.1 ProjectTopic

```java
package com.training.system.topic.entity;

import java.time.LocalDateTime;

public class ProjectTopic {
    private Long topicId;
    private String topicName;
    private String topicType;
    private String difficulty;
    private Long teacherId;
    private Integer studentLimit;
    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;
    private Integer teamLimit;
    private String topicContent;
    private String developTools;
    private String technicalRoute;
    private Integer status;
    private Integer openStatus;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;
    // getters and setters ...
}
```

### 4.2 TopicFile

```java
package com.training.system.topic.entity;

import java.time.LocalDateTime;

public class TopicFile {
    private Long fileId;
    private Long topicId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
    private String fileDesc;
    private Integer isDeleted;
    // getters and setters ...
}
```

### 4.3 TopicReview

```java
package com.training.system.topic.entity;

import java.time.LocalDateTime;

public class TopicReview {
    private Long reviewId;
    private Long topicId;
    private Long adminId;
    private Integer reviewResult;
    private String reviewComment;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    // getters and setters ...
}
```

## 5. DTO/VO 与数据表映射关系

| DTO/VO | 对应表/来源 | 方向 |
|---|---|---|
| `TopicCreateDTO` | `project_topic` | 请求 → 服务层 |
| `TopicUpdateDTO` | `project_topic` | 请求 → 服务层 |
| `TopicQueryDTO` | `project_topic` 查询条件 | 请求 → 服务层 |
| `TopicReviewDTO` | `topic_review` | 请求 → 服务层 |
| `TopicListVO` | `project_topic` + `teacher` | 服务层 → 响应 |
| `TopicDetailVO` | `project_topic` + `teacher` + `topic_file` + `topic_review` | 服务层 → 响应 |
| `TopicFileVO` | `topic_file` + `user_account` | 服务层 → 响应 |
| `TopicReviewVO` | `topic_review` + `user_account` | 服务层 → 响应 |
