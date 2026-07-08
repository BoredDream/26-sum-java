package com.training.system.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 补签审核请求参数
 */
public class MakeupReviewDTO {

    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    private String auditComment;

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }
}
