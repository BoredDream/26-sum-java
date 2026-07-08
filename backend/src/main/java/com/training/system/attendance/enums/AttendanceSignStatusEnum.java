package com.training.system.attendance.enums;

/**
 * 签到状态枚举
 */
public enum AttendanceSignStatusEnum {

    ABSENT(0, "缺勤"),
    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    MAKEUP(3, "补签");

    private final int code;
    private final String desc;

    AttendanceSignStatusEnum(int code, String desc) {
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
        for (AttendanceSignStatusEnum value : values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
