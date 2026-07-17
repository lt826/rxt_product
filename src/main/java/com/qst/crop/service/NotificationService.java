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
     * 标记某用户的全部通知为已读（点击铃铛即消除红点）
     *
     * @return 被标记为已读的条数
     */
    int markAllAsRead(String userName);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(String userName);

    /**
     * 向某一角色的所有已审批通过用户广播通知（如"通知所有买家/农户/管理员/银行"）
     *
     * @param role     角色：farmer / buyer / bank / expert / admin
     * @param relatedId 关联业务id，可为 null
     * @return 实际发送的通知条数
     */
    int sendToRole(String role, String title, String content, String type, Integer relatedId);
}
