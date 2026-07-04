package com.training.system.selection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LogFeedbackDTO {
    @NotBlank(message = "反馈意见不能为空")
    @Size(max = 1000, message = "反馈意见不能超过1000个字符")
    private String feedback;

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
