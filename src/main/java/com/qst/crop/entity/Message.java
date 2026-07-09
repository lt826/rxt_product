package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 买家-农户消息表实体类
 */
@Data
@TableName("tb_message")
public class Message {

    /** 消息ID */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    /** 发送方用户名 */
    private String fromUser;

    /** 接收方用户名 */
    private String toUser;

    /** 消息内容 */
    private String content;

    /** 是否已读(0:未读,1:已读) */
    private Integer isRead;

    /** 创建时间 */
    private LocalDateTime createTime;
}
