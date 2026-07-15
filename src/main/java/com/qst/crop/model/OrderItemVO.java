package com.qst.crop.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品明细 VO
 */
@Data
public class OrderItemVO {
    /** 商品id */
    private Integer goodsId;
    /** 商品标题 */
    private String title;
    /** 单价 */
    private BigDecimal uninPrice;
    /** 数量 */
    private Integer count;
    /** 小计 */
    private BigDecimal sumPrice;
}
