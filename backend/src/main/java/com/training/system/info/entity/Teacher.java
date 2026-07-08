package com.training.system.info.entity;

import com.training.system.info.annotation.ExcelColumn;
import java.time.LocalDateTime;

/**
 * 教师实体
 * 对应数据库表 teacher
 */
public class Teacher {

    private Long teacherId;

    @ExcelColumn(index = 0, header = "工号")
    private String teacherNo;

    @ExcelColumn(index = 1, header = "姓名")
    private String teacherName;

    @ExcelColumn(index = 2, header = "教研室")
    private String office;

    @ExcelColumn(index = 3, header = "职称")
    private String title;

    @ExcelColumn(index = 4, header = "手机号")
    private String phone;

    @ExcelColumn(index = 5, header = "邮箱")
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
