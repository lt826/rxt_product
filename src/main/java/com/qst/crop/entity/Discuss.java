package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识讨论表实体类
 */
@Data
@TableName("tb_discuss")
public class Discuss {

    /** 讨论id */
    @TableId(value = "discuss_id", type = IdType.AUTO)
    private Integer discussId;

    /** 知识id */
    private Integer knowledgeId;

    /** 讨论内容 */
    private String content;

    /** 发布者名字 */
    private String ownName;

    /** 创建时间 */
    private LocalDateTime createTime;
}