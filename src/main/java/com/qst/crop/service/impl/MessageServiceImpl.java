package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.MessageMapper;
import com.qst.crop.entity.Message;
import com.qst.crop.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> getConversation(String fromUser, String toUser) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w
                .eq("from_user", fromUser).eq("to_user", toUser)
                .or().eq("from_user", toUser).eq("to_user", fromUser))
                .orderByAsc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Integer getUnreadCount(String toUser, String fromUser) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user", toUser)
                .eq("from_user", fromUser)
                .eq("is_read", 0);
        return messageMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void markAsRead(String toUser, String fromUser) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user", toUser)
                .eq("from_user", fromUser)
                .eq("is_read", 0);
        Message update = new Message();
        update.setIsRead(1);
        messageMapper.update(update, wrapper);
    }

    @Override
    public List<Message> getUnreadMessages(String toUser) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user", toUser)
                .eq("is_read", 0)
                .orderByDesc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Message sendMessage(String fromUser, String toUser, String content) {
        Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }
}
