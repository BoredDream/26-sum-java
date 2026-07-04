package com.training.system.info.entity;

import java.time.LocalDateTime;

/**
 * 教师实体
 * 对应数据库表 teacher
 */
public class Teacher {

    private Long teacherId;
    private String teacherNo;
    private String teacherName;
    private String office;
    private String title;
    private String phone;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
