package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Notification;
import com.qst.crop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息控制器
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping("/list")
    public Result<List<Notification>> getMyNotifications(Authentication auth) {
        String userName = auth.getName();
        List<Notification> list = notificationService.getUserNotifications(userName);
        return Result.success("获取成功", list);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(Authentication auth) {
        String userName = auth.getName();
        long count = notificationService.getUnreadCount(userName);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success("获取成功", data);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Integer id) {
        boolean ok = notificationService.markAsRead(id);
        if (ok) {
            return Result.success("标记成功", true);
        } else {
            return Result.error(404, "消息不存在");
        }
    }

    /**
     * 标记当前用户的全部通知为已读（点击铃铛即消除红点）
     */
    @PutMapping("/read-all")
    public Result<Map<String, Object>> markAllAsRead(Authentication auth) {
        String userName = auth.getName();
        int updated = notificationService.markAllAsRead(userName);
        Map<String, Object> data = new HashMap<>();
        data.put("updated", updated);
        return Result.success("已全部标记为已读", data);
    }
}
