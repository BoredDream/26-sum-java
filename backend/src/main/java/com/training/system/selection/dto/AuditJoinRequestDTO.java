package com.training.system.selection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuditJoinRequestDTO {
    @NotNull(message = "请指定是否通过")
    private Boolean approved;

    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String opinion;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
}
