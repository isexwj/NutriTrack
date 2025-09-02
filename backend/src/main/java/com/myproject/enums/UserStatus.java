package com.myproject.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户状态枚举
 * 用于对应数据库 `users` 表中的 `status` 字段
 */
public enum UserStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    SUSPENDED("suspended"),
    DELETED("deleted");

    @EnumValue // 标记数据库存的值是code
    @JsonValue // 标记响应json序列化时返回此字段
    private final String code;

    UserStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}