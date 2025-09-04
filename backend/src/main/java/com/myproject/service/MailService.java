package com.myproject.service;

public interface MailService {

    /**
     * 发送简单文本邮件
     * @param to 收件邮箱
     * @param subject 主题
     * @param content 文本内容
     */
    void sendTextMail(String to, String subject, String content);
}


