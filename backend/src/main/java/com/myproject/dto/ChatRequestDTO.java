/**
 * 文件路径: src/main/java/com/myproject/dto/ChatRequestDTO.java
 * 说明: 前端发送的聊天请求 DTO，仅包含用户的问题文本
 */

package com.myproject.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    /**
     * 用户要问的问题，例如 "我今天晚餐吃了什么？有减肥建议吗？"
     */
    private String question;

    // 可选：前端可传 username（用于后端从 username 解析 userId）
    private String username;

    private Long userId;
}
