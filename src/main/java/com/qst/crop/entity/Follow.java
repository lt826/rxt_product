package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系表实体类（买家关注农户）
 */
@Data
@TableName("tb_follow")
public class Follow {

    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关注者用户名（买家） */
    private String follower;

    /** 被关注者用户名（农户） */
    private String followee;

    /** 创建时间 */
    private LocalDateTime createTime;
}
