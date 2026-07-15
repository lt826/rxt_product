package com.qst.crop.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单 VO（买家"我买到的" / 卖家"我卖出的"共用）
 */
@Data
public class OrderVO {
    /** 订单id */
    private Integer purchaseId;
    /** 订单状态：2待发货，3待收货，4已收货 */
    private Integer status;
    /** 状态文案 */
    private String statusText;
    /** 总价 */
    private BigDecimal totalPrice;
    /** 创建时间 */
    private String createTime;

    /** 收货人姓名 */
    private String consignee;
    /** 收货人电话 */
    private String consigneePhone;
    /** 收货地址 */
    private String address;

    /** 发货人姓名 */
    private String shipName;
    /** 发货人电话 */
    private String shipPhone;
    /** 发货地址 */
    private String shipAddress;

    /** 商品明细 */
    private List<OrderItemVO> items;
}
