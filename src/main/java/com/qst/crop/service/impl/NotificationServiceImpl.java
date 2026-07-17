package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qst.crop.dao.NotificationMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Notification;
import com.qst.crop.entity.User;
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

    @Autowired
    private UserMapper userMapper;

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
    public int markAllAsRead(String userName) {
        if (userName == null || userName.isEmpty()) {
            return 0;
        }
        // 只把未读的置为已读，返回受影响行数
        return notificationMapper.update(null, new UpdateWrapper<Notification>()
                .eq("receiver", userName)
                .eq("is_read", 0)
                .set("is_read", 1));
    }

    @Override
    public long getUnreadCount(String userName) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver", userName);
        wrapper.eq("is_read", 0);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public int sendToRole(String role, String title, String content, String type, Integer relatedId) {
        if (role == null || role.isEmpty()) {
            return 0;
        }
        // 按角色查询全部用户并逐个发送。
        // 注：tb_user 的 status 列在不同部署里可能缺失，故此处不依赖 status 过滤；
        // pending/rejected 用户即使收到通知也无法登录查看，无副作用。
        QueryWrapper<User> uw = new QueryWrapper<>();
        uw.eq("role", role);
        List<User> users = userMapper.selectList(uw);
        int count = 0;
        for (User u : users) {
            if (u.getUserName() == null) continue;
            send(u.getUserName(), title, content, type, relatedId);
            count++;
        }
        log.info("角色广播通知: role={}, title={}, 发送={} 条", role, title, count);
        return count;
    }
}
