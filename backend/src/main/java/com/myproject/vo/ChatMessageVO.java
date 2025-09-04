/**
 * 文件路径: src/main/java/com/myproject/vo/ChatMessageVO.java
 * 说明: 返回给前端的单条聊天消息 VO（只包含 AI 的回复，不包含内部的前置知识）
 */

package com.myproject.vo;

import lombok.Data;

@Data
public class ChatMessageVO {
    private String id;        // 消息ID（可用UUID）
    private String type;      // "ai" 或 "user"（当前返回为 ai）
    private String content;   // AI 的回答文本（供前端展示）
    private String timestamp; // ISO 时间字符串
}
