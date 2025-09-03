package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/MealImage.java
 * 说明: 对应数据库表 meal_images（字段一一对应）
 */
@Data
@TableName("meal_images")
public class MealImage {
    @TableId
    @TableField("id")
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("upload_order")
    private Integer uploadOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}