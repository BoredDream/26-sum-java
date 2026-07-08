package com.training.system.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 在线签到请求参数
 */
public class AttendanceSignDTO {

    @NotNull(message = "签到任务ID不能为空")
    private Long taskId;

    private String remark;

    private Double signLng;
    private Double signLat;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getSignLng() {
        return signLng;
    }

    public void setSignLng(Double signLng) {
        this.signLng = signLng;
    }

    public Double getSignLat() {
        return signLat;
    }

    public void setSignLat(Double signLat) {
        this.signLat = signLat;
    }
}
