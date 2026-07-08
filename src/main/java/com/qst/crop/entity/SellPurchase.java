package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卖家出货订单表实体类
 */
@Data
@TableName("tb_sell_purchase")
public class SellPurchase {

    /** 卖出订单id */
    @TableId(value = "sell_purchase_id", type = IdType.AUTO)
    private Integer sellPurchaseId;

    /** 全局订单id */
    private Integer purchaseId;

    /** 卖家用户名 */
    private String ownName;

    /** 类型，2卖出的 */
    private Integer purchaseType;

    /** 单价 */
    private BigDecimal uninPrice;

    /** 总价格 */
    private BigDecimal sumPrice;

    /** 发货/收货地址 */
    private String address;

    /** 订单状态，1待付款，2待发货，3待收货，4已收货 */
    private Integer purchaseStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 商品id */
    private Integer goodsId;
}