package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车表实体类
 */
@Data
@TableName("tb_shoppingcart")
public class Shoppingcart {

    /** 购物车id */
    @TableId(value = "shopping_id", type = IdType.AUTO)
    private Integer shoppingId;

    /** 商品id */
    private Integer goodsId;

    /** 数量 */
    private Integer count;

    /** 用户 */
    private String ownName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}