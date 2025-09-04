package com.myproject.service.impl;

import com.myproject.service.MailService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${mail.from:no-reply@example.com}")
    private String from;

    @Value("${spring.mail.username:}")
    private String smtpUser;

    @Override
    public void sendTextMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        // 绝大多数 SMTP 服务商要求发件人与认证用户一致
        String fromAddress = (smtpUser != null && !smtpUser.isEmpty()) ? smtpUser : from;
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        // TODO: 如需异步发送或模板邮件，请在此扩展
        mailSender.send(message);
    }
}


