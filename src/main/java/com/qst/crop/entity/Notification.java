package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知消息表实体类
 */
@Data
@TableName("tb_notification")
public class Notification {

    /** 消息id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 接收人用户名 */
    private String receiver;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型 */
    private String type;

    /** 是否已读 */
    private Integer isRead;

    /** 关联id */
    private Integer relatedId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
