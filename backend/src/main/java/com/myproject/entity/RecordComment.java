package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/RecordComment.java
 * 说明: 对应数据库表 record_comments（字段一一对应）
 */
@Data
@TableName("record_comments")
public class RecordComment {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("parent_comment_id")
    private Long parentCommentId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
