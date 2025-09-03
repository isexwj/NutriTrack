package com.myproject.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 文件路径: src/main/java/com/myproject/enums/NotificationType.java
 * 说明: 对应数据库 notifications.notification_type 列
 * SQL 定义: enum('meal_reminder','system','like','comment')
 */
public enum NotificationType {
    MEAL_REMINDER("meal_reminder"),
    SYSTEM("system"),
    LIKE("like"),
    COMMENT("comment");

    @EnumValue
    @JsonValue
    private final String code;

    NotificationType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
