package com.myproject.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 文件路径: src/main/java/com/myproject/enums/MealType.java
 * 说明: 对应数据库 meal_records.meal_type 列的枚举
 * 值与 SQL 中 enum 定义严格对应: 'breakfast','lunch','dinner','snack'
 */
public enum MealType {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner"),
    SNACK("snack");

    @EnumValue
    @JsonValue
    private final String code;

    MealType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}