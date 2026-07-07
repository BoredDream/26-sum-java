package com.training.system.selection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubmitSelectionDTO {
    @NotNull(message = "课题编号不能为空")
    private Long topicId;

    @NotBlank(message = "选题说明不能为空")
    @Size(max = 1000, message = "选题说明不能超过1000个字符")
    private String selectionReason;

    @NotNull(message = "请指定团队")
    private Long teamId;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getSelectionReason() { return selectionReason; }
    public void setSelectionReason(String selectionReason) { this.selectionReason = selectionReason; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
