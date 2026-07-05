package com.training.system.attendance.vo;

import java.math.BigDecimal;

/**
 * 考勤统计结果
 */
public class AttendanceStatisticsVO {

    private Integer totalCount;
    private Integer normalCount;
    private Integer lateCount;
    private Integer absentCount;
    private Integer makeupCount;
    private BigDecimal attendanceRate;
    private Integer lateTimes;
    private Integer absentTimes;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(Integer normalCount) {
        this.normalCount = normalCount;
    }

    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    public Integer getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(Integer absentCount) {
        this.absentCount = absentCount;
    }

    public Integer getMakeupCount() {
        return makeupCount;
    }

    public void setMakeupCount(Integer makeupCount) {
        this.makeupCount = makeupCount;
    }

    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public Integer getLateTimes() {
        return lateTimes;
    }

    public void setLateTimes(Integer lateTimes) {
        this.lateTimes = lateTimes;
    }

    public Integer getAbsentTimes() {
        return absentTimes;
    }

    public void setAbsentTimes(Integer absentTimes) {
        this.absentTimes = absentTimes;
    }
}
