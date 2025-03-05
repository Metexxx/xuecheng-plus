package com.xuecheng.base.model.enums;

public enum CourseType {
    FREE("700001", "免费课程"),
    CHARGE("700002", "收费课程");

    private final String code;
    private final String description;

    CourseType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CourseType fromCode(String code) {
        for (CourseType type : CourseType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid course type code: " + code);
    }
}
