package com.training.system.attendance.enums;

/**
 * 签到任务类型枚举
 */
public enum AttendanceTaskTypeEnum {

    DAILY(1, "日常签到"),
    STAGE(2, "阶段签到");

    private final int code;
    private final String desc;

    AttendanceTaskTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (AttendanceTaskTypeEnum value : values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
