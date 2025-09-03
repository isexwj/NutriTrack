package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.myproject.enums.MealType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/MealRecord.java
 * 说明: 对应数据库表 meal_records（字段一一对应）
 * 注意: meal_type 使用枚举 MealType，与 SQL enum('breakfast','lunch','dinner','snack') 一致
 */
@Data
@TableName("meal_records")
public class MealRecord {
    @TableId
    @TableField("id")
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("meal_type")
    private MealType mealType;

    @TableField("description")
    private String description;

    @TableField("calories")
    private Integer calories;

    @TableField("rating")
    private BigDecimal rating;

    @TableField("record_date")
    private LocalDate recordDate;

    @TableField("is_shared")
    private Integer isShared; // tinyint(1)

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}