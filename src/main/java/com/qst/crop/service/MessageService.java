package com.qst.crop.service;

import com.qst.crop.entity.Message;
import java.util.List;

public interface MessageService {
    /** 获取两人之间的对话历史 */
    List<Message> getConversation(String fromUser, String toUser);

    /** 获取某人发给某人的未读消息数量 */
    Integer getUnreadCount(String toUser, String fromUser);

    /** 标记来自某人的所有未读消息为已读 */
    void markAsRead(String toUser, String fromUser);

    /** 获取某人收到的所有未读消息 */
    List<Message> getUnreadMessages(String toUser);

    /** 发送消息 */
    Message sendMessage(String fromUser, String toUser, String content);
}
