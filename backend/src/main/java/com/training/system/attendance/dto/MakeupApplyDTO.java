package com.training.system.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 提交补签申请请求参数
 */
public class MakeupApplyDTO {

    @NotNull(message = "签到任务ID不能为空")
    private Long taskId;

    private Long recordId;

    @NotBlank(message = "请填写补签原因")
    @Size(min = 5, max = 500, message = "补签原因长度为5-500字符")
    private String applyReason;

    private String proofFilePath;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getProofFilePath() {
        return proofFilePath;
    }

    public void setProofFilePath(String proofFilePath) {
        this.proofFilePath = proofFilePath;
    }
}
