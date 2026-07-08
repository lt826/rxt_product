package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品/供求信息表实体类
 */
@Data
@TableName("tb_goods")
public class Goods {

    /** 商品/供求id */
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Integer goodsId;

    /** 标题 */
    private String title;

    /** 期望价格 */
    private BigDecimal price;

    /** 内容详情 */
    private String content;

    /** 状态 */
    private Integer goodsStatus;

    /** 类型，1销售供应，2采购需求 */
    private String type;

    /** 图片 */
    private String picture;

    /** 发布者 */
    private String ownName;

    /** 合作人名字 */
    private String cooperationName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 交易/产地地址 */
    private String address;
}