package com.training.system.info.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改教师请求参数
 */
public class TeacherUpdateDTO {

    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    @NotBlank(message = "姓名不能为空")
    private String teacherName;

    private String office;
    private String title;
    private String phone;
    private String email;

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
}
