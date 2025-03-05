package com.xuecheng.base.model.enums;

public enum LearningStatus {
    NORMAL_LEARNING("702001", "正常学习"),
    NO_SELECTION_OR_PAYMENT("702002", "没有选课或选课后没有支付"),
    EXPIRED_NEED_RENEWAL_OR_PAYMENT("702003", "已过期需要申请续期或重新支付");

    private final String code;
    private final String description;

    LearningStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LearningStatus fromCode(String code) {
        for (LearningStatus status : LearningStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid learning status code: " + code);
    }
}
