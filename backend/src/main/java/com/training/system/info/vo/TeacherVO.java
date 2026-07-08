package com.training.system.info.vo;

import java.time.LocalDateTime;

/**
 * 教师视图对象
 */
public class TeacherVO {

    private Long teacherId;
    private String teacherNo;
    private String teacherName;
    private String office;
    private String title;
    private String phone;
    private String email;
    private String role;
    private String roleText;
    private Integer status;
    private LocalDateTime createTime;

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
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRoleText() { return roleText; }
    public void setRoleText(String roleText) { this.roleText = roleText; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
