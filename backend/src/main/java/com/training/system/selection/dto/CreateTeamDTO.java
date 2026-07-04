package com.training.system.selection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTeamDTO {
    @NotBlank(message = "团队名称不能为空")
    @Size(max = 100, message = "团队名称不能超过100个字符")
    private String teamName;

    @Size(max = 500, message = "团队简介不能超过500个字符")
    private String introduction;

    @Min(value = 1, message = "团队人数上限至少为1")
    @Max(value = 10, message = "团队人数上限不能超过10")
    private Integer maxSize;

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public Integer getMaxSize() { return maxSize; }
    public void setMaxSize(Integer maxSize) { this.maxSize = maxSize; }
}
