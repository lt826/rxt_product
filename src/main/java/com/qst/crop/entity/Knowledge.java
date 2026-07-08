package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农业知识表实体类
 */
@Data
@TableName("tb_knowledge")
public class Knowledge {

    /** 知识id */
    @TableId(value = "knowledge_id", type = IdType.AUTO)
    private Integer knowledgeId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 视频图片路径 */
    private String picPath;

    /** 发布者名字 */
    private String ownName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}