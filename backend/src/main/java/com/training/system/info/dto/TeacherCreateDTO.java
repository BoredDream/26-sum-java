package com.training.system.info.dto;

/**
 * 新增教师请求参数
 */
public class TeacherCreateDTO {

    private String teacherNo;
    private String teacherName;
    private String office;
    private String title;
    private String phone;
    private String email;
    private String password;

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
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
