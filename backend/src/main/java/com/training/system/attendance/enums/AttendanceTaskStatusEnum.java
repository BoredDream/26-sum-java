package com.training.system.attendance.enums;

/**
 * 签到任务状态枚举
 */
public enum AttendanceTaskStatusEnum {

    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    FINISHED(2, "已结束");

    private final int code;
    private final String desc;

    AttendanceTaskStatusEnum(int code, String desc) {
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
        for (AttendanceTaskStatusEnum value : values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
