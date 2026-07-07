package com.training.system.selection.dto;

import jakarta.validation.constraints.Size;

public class RequestLeaveTeamDTO {
    @Size(max = 500, message = "离队原因不能超过500个字符")
    private String leaveMessage;

    public String getLeaveMessage() { return leaveMessage; }
    public void setLeaveMessage(String leaveMessage) { this.leaveMessage = leaveMessage; }
}
