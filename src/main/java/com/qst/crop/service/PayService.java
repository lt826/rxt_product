package com.qst.crop.service;

import com.qst.crop.model.PayQrcodeVO;

/**
 * 支付宝沙箱支付服务
 */
public interface PayService {

    /**
     * 为订单生成支付宝扫码支付二维码（预下单）。
     * 凭证缺失或调用失败时返回 qrCode=null 的对象，不抛异常。
     */
    PayQrcodeVO createQrcode(Integer purchaseId, String buyerName);

    /**
     * 查询订单支付状态；若已支付则结算订单（转为待发货）。
     */
    boolean queryAndSettle(Integer purchaseId, String outTradeNo, String buyerName);
}
