package com.training.system.attendance.enums;

/**
 * 签到任务适用范围枚举
 */
public enum AttendanceScopeTypeEnum {

    CLASS(1, "班级"),
    TEAM(2, "团队"),
    ALL(3, "全部");

    private final int code;
    private final String desc;

    AttendanceScopeTypeEnum(int code, String desc) {
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
        for (AttendanceScopeTypeEnum value : values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
