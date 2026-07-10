package com.qst.crop.service;

import com.qst.crop.entity.Notification;

import java.util.List;

/**
 * 通知消息服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    Notification send(String receiver, String title, String content, String type, Integer relatedId);

    /**
     * 获取用户的通知列表
     */
    List<Notification> getUserNotifications(String userName);

    /**
     * 标记为已读
     */
    boolean markAsRead(Integer id);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(String userName);
}
