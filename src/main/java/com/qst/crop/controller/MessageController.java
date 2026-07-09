package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Message;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 买家-农户消息控制器
 */
@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /** 获取与指定用户的对话历史 */
    @GetMapping("/conversation")
    public Result<List<Message>> conversation(@RequestParam String toUser) {
        String currentUser = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", messageService.getConversation(currentUser, toUser));
    }

    /** 获取未读消息统计（按发送方分组） */
    @GetMapping("/unread-summary")
    public Result<List<Map<String, Object>>> unreadSummary() {
        String currentUser = SecurityUtil.getCurrentUsername();
        // 获取所有发送给当前用户的未读消息
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Message> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("to_user", currentUser).eq("is_read", 0)
                .orderByDesc("create_time");
        List<Message> unreadMessages = messageService.getUnreadMessages(currentUser);
        // 按发送方分组
        Map<String, Integer> unreadBySender = new java.util.LinkedHashMap<>();
        Map<String, Message> latestBySender = new java.util.LinkedHashMap<>();
        for (Message msg : unreadMessages) {
            String sender = msg.getFromUser();
            unreadBySender.merge(sender, 1, Integer::sum);
            latestBySender.putIfAbsent(sender, msg);
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> entry : unreadBySender.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("sender", entry.getKey());
            item.put("unreadCount", entry.getValue());
            Message latest = latestBySender.get(entry.getKey());
            if (latest != null) {
                item.put("lastContent", latest.getContent());
                item.put("lastTime", latest.getCreateTime());
            }
            result.add(item);
        }
        return Result.success("查询成功", result);
    }

    /** 发送消息 */
    @PostMapping("/send")
    public Result<Message> send(@RequestBody Map<String, String> body) {
        String fromUser = SecurityUtil.getCurrentUsername();
        String toUser = body.get("toUser");
        String content = body.get("content");
        if (toUser == null || content == null || content.trim().isEmpty()) {
            return Result.error(400, "参数不完整");
        }
        Message message = messageService.sendMessage(fromUser, toUser, content.trim());
        return Result.success("发送成功", message);
    }

    /** 标记来自某人的所有消息为已读 */
    @PutMapping("/read/{fromUser}")
    public Result<Void> markAsRead(@PathVariable String fromUser) {
        String toUser = SecurityUtil.getCurrentUsername();
        messageService.markAsRead(toUser, fromUser);
        return Result.success("已标记", null);
    }
}
