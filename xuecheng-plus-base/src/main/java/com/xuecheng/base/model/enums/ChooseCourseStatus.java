package com.xuecheng.base.model.enums;

public enum ChooseCourseStatus {
    SUCCESS("701001", "选课成功"),
    PENDING_PAYMENT("701002", "待支付");

    private final String code;
    private final String description;

    ChooseCourseStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ChooseCourseStatus fromCode(String code) {
        for (ChooseCourseStatus status : ChooseCourseStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid choose course status code: " + code);
    }
}