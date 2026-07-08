package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单详情表实体类
 */
@Data
@TableName("tb_purchase_detail")
public class PurchaseDetail {

    /** 明细id */
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Integer detailId;

    /** 订单id */
    private Integer purchaseId;

    /** 商品id */
    private Integer goodsId;

    /** 单价 */
    private BigDecimal uninPrice;

    /** 总价格 */
    private BigDecimal sumPrice;

    /** 数量 */
    private Integer count;
}