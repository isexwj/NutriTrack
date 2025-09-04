package com.myproject.controller;

import com.myproject.result.ResponseResult;
import com.myproject.service.UserService;
import com.myproject.entity.Notification;
import com.myproject.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final UserService userService;

    @GetMapping
    public ResponseResult<List<Notification>> list(@RequestParam String username) {
        Long userId = userService.getUserInfo(username).getData().getId();
        return ResponseResult.success(notificationMapper.listByUserId(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseResult<String> markRead(@PathVariable Long id) {
        return notificationMapper.markRead(id) > 0
                ? ResponseResult.success("已标记已读")
                : ResponseResult.fail("标记失败");
    }

    @PostMapping("/read-all")
    public ResponseResult<String> markAllRead(@RequestParam String username) {
        Long userId = userService.getUserInfo(username).getData().getId();
        int updated = notificationMapper.markAllRead(userId);
        return ResponseResult.success("已读数量:" + updated);
    }
}


