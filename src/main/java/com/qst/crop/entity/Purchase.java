package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 买家交易订单表实体类
 */
@Data
@TableName("tb_purchase")
public class Purchase {

    /** 订单id */
    @TableId(value = "purchase_id", type = IdType.AUTO)
    private Integer purchaseId;

    /** 买家用户名 */
    private String ownName;

    /** 类型，1买到的，2卖出的 */
    private Integer purchaseType;

    /** 总价格 */
    private BigDecimal totalPrice;

    /** 收货地址 */
    private String address;

    /** 收货人姓名 */
    private String consignee;

    /** 收货人电话 */
    private String consigneePhone;

    /** 订单状态，1待付款，2待发货，3待收货，4已收货 */
    private Integer purchaseStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}