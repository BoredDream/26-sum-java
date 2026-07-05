package com.training.system.attendance.vo;

import java.util.List;

/**
 * 签到任务详情
 */
public class AttendanceTaskDetailVO extends AttendanceTaskVO {

    private String description;
    private List<AttendanceRecordVO> records;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttendanceRecordVO> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordVO> records) {
        this.records = records;
    }
}
