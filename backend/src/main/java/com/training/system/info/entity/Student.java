package com.training.system.info.entity;

import com.training.system.info.annotation.ExcelColumn;
import java.time.LocalDateTime;

/**
 * 学生实体
 * 对应数据库表 student
 */
public class Student {

    private Long studentId;

    @ExcelColumn(index = 0, header = "学号")
    private String studentNo;

    @ExcelColumn(index = 1, header = "姓名")
    private String studentName;

    @ExcelColumn(index = 2, header = "专业")
    private String major;

    @ExcelColumn(index = 3, header = "班级")
    private String className;

    @ExcelColumn(index = 4, header = "手机号")
    private String phone;

    @ExcelColumn(index = 5, header = "邮箱")
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
