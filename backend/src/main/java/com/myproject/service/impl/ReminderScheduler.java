package com.myproject.service.impl;

import com.myproject.entity.Notification;
import com.myproject.enums.NotificationType;
import com.myproject.mapper.NotificationMapper;
import com.myproject.mapper.UserMapper;
import com.myproject.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    // 早餐 08:00
    @Scheduled(cron = "0 0 7 * * ?")
    public void breakfastReminder() {
        sendSystemReminder("早餐时间到啦，记得记录今天的早餐哦~");
    }

    // 午餐 12:00
    @Scheduled(cron = "0 30 11 * * ?")
    public void lunchReminder() {
        sendSystemReminder("午餐时间到啦，别忘了打卡记录！");
    }

    // 晚餐 18:00
    @Scheduled(cron = "0 30 17 * * ?")
    public void dinnerReminder() {
        sendSystemReminder("晚餐时间到啦，保持清淡适量，及时记录~");
    }

    // 测试 Reminder，每天16：54分发通知(测试通过)
//    @Scheduled(cron = "0 54 16 * * ?")
//    public void testReminder() {
//        sendSystemReminder("晚餐时间到啦，保持清淡适量，及时记录~");
//    }

    private void sendSystemReminder(String content) {
        List<User> users = userMapper.selectList(null);
        for (User u : users) {
            Notification n = new Notification();
            n.setUserId(u.getId());
            n.setSenderUserId(2L); // 约定 system 用户ID=1
            n.setNotificationType(NotificationType.MEAL_REMINDER);
            n.setTargetRecordId(null);
            n.setContent(content);
            n.setIsRead(0);
            n.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(n);
        }
    }
}


