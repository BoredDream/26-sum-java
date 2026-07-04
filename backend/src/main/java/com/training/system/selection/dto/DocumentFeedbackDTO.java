package com.training.system.selection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentFeedbackDTO {
    @NotBlank(message = "反馈意见不能为空")
    @Size(max = 1000, message = "反馈意见不能超过1000个字符")
    private String feedback;

    @NotNull(message = "请指定是否退回")
    private Boolean returned;

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Boolean getReturned() { return returned; }
    public void setReturned(Boolean returned) { this.returned = returned; }
}
