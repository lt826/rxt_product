package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.NotificationMapper;
import com.qst.crop.entity.Notification;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知消息服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public Notification send(String receiver, String title, String content, String type, Integer relatedId) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("通知已发送: receiver={}, title={}, type={}", receiver, title, type);
        return notification;
    }

    @Override
    public List<Notification> getUserNotifications(String userName) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver", userName);
        wrapper.orderByDesc("create_time");
        return notificationMapper.selectList(wrapper);
    }

    @Override
    public boolean markAsRead(Integer id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) return false;

        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        return true;
    }

    @Override
    public long getUnreadCount(String userName) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver", userName);
        wrapper.eq("is_read", 0);
        return notificationMapper.selectCount(wrapper);
    }
}
