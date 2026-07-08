package com.training.system.attendance.dto;

/**
 * 当前登录用户信息
 */
public class CurrentUserDTO {

    private Long userId;
    private String role;
    private Long relatedId;

    public CurrentUserDTO() {
    }

    public CurrentUserDTO(Long userId, String role, Long relatedId) {
        this.userId = userId;
        this.role = role;
        this.relatedId = relatedId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
