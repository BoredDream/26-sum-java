package com.training.system.selection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateMemberWorkDTO {
    @NotBlank(message = "成员分工不能为空")
    @Size(max = 500, message = "成员分工不能超过500个字符")
    private String workContent;

    public String getWorkContent() { return workContent; }
    public void setWorkContent(String workContent) { this.workContent = workContent; }
}
