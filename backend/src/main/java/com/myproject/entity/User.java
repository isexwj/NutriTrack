package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.myproject.enums.UserStatus; // 引入新的枚举类
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库中的 `users` 表
 */
@Data
@TableName("users") // 对应数据库中的 'users' 表
public class User {

    /**
     * 用户唯一ID (主键,自增)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号 (数据库字段: phone_number)
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像图片URL (数据库字段: avatar_url)
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 用户状态: active-活跃, inactive-未激活, suspended-被封禁, deleted-已注销
     * MyBatis-Plus会自动将枚举名映射到数据库的ENUM类型
     */
    private UserStatus status;

    /**
     * 注册时间 (数据库字段: created_at, 插入时自动填充)
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 信息更新时间 (数据库字段: updated_at, 插入和更新时自动填充)
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 注销时间 (数据库字段: deleted_at)
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}