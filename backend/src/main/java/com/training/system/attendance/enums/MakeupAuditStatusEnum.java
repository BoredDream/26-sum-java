package com.training.system.attendance.enums;

/**
 * 补签审核状态枚举
 */
public enum MakeupAuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回");

    private final int code;
    private final String desc;

    MakeupAuditStatusEnum(int code, String desc) {
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
        for (MakeupAuditStatusEnum value : values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
