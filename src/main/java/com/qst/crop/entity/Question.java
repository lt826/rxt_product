package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 在线问答表实体类
 */
@Data
@TableName("tb_question")
public class Question {

    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 专家用户名 */
    private String expertName;

    /** 咨询者用户名 */
    private String questioner;

    /** 手机号 */
    private String phone;

    /** 农作物名称 */
    private String plantName;

    /** 问题标题 */
    private String title;

    /** 问题描述 */
    private String question;

    /** 专家回答 */
    private String answer;

    /** 状态，0：提问中，1：回答完毕 */
    private Integer status;
}