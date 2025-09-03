package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/RecordLike.java
 * 说明: 对应数据库表 record_likes（字段一一对应）
 */
@Data
@TableName("record_likes")
public class RecordLike {
    @TableId
    @TableField("id")
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("user_id")
    private Long userId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}