package com.training.system.selection.dto;

import jakarta.validation.constraints.Size;

public class JoinTeamDTO {
    @Size(max = 500, message = "申请说明不能超过500个字符")
    private String applyMessage;

    public String getApplyMessage() { return applyMessage; }
    public void setApplyMessage(String applyMessage) { this.applyMessage = applyMessage; }
}
