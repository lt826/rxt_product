package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答对话消息表实体类（农户与专家持续聊天）
 */
@Data
@TableName("tb_question_message")
public class QuestionMessage {

    /** 消息id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 所属问题id */
    private Integer questionId;

    /** 发送者用户名 */
    private String sender;

    /** 发送者角色(farmer/expert) */
    private String senderRole;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
