package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.myproject.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/Notification.java
 * 说明: 对应数据库表 notifications（字段一一对应）
 * 注意: notification_type 使用枚举 NotificationType
 */
@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("sender_user_id")
    private Long senderUserId;

    @TableField("notification_type")
    private NotificationType notificationType;

    @TableField("target_record_id")
    private Long targetRecordId;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead; // tinyint(1)

    @TableField("created_at")
    private LocalDateTime createdAt;
}
