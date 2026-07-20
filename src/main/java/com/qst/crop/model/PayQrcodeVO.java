package com.qst.crop.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付二维码返回对象
 */
@Data
public class PayQrcodeVO {

    /** 订单id */
    private Integer purchaseId;

    /** 支付宝交易号（预下单生成，用于后续查询） */
    private String outTradeNo;

    /** 二维码内容（前端用 qrcode 库渲染成图片）；凭证缺失/失败时为 null */
    private String qrCode;

    /** 失败时的错误信息（预下单失败原因直接透传，供前端提示） */
    private String errorMsg;

    /** 应付金额 */
    private BigDecimal totalPrice;
}
