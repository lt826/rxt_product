package com.qst.crop.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.qst.crop.dao.PurchaseMapper;
import com.qst.crop.entity.Purchase;
import com.qst.crop.model.PayQrcodeVO;
import com.qst.crop.service.PayService;
import com.qst.crop.service.PurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 支付宝沙箱支付服务实现：预下单生成二维码 + 查询支付状态并结算。
 */
@Slf4j
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private PurchaseService purchaseService;

    @Override
    public PayQrcodeVO createQrcode(Integer purchaseId, String buyerName) {
        PayQrcodeVO vo = new PayQrcodeVO();
        vo.setPurchaseId(purchaseId);

        Purchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null || !buyerName.equals(purchase.getOwnName())) {
            log.warn("生成支付二维码失败：订单不存在或无权操作, purchaseId={}", purchaseId);
            return vo;
        }
        vo.setTotalPrice(purchase.getTotalPrice());

        // 每次生成新的 outTradeNo，避免重复预下单触发 TRADE_EXIST
        String outTradeNo = "rxt_" + purchaseId + "_" + System.currentTimeMillis();
        vo.setOutTradeNo(outTradeNo);

        String amount = purchase.getTotalPrice() != null
                ? purchase.getTotalPrice().setScale(2, RoundingMode.HALF_UP).toPlainString()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP).toPlainString();

        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(amount);
            model.setSubject("融销通订单#" + purchaseId);
            request.setBizModel(model);

            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response != null && response.isSuccess()) {
                vo.setQrCode(response.getQrCode());
            } else {
                String code = response != null ? response.getCode() : "null";
                String msg = response != null ? response.getMsg() : "无响应";
                String subCode = response != null ? response.getSubCode() : "";
                String subMsg = response != null ? response.getSubMsg() : "";
                vo.setErrorMsg(String.format("%s : %s（%s : %s）", code, msg, subCode, subMsg));
                log.warn("支付宝预下单失败：code={}, msg={}, subCode={}, subMsg={}", code, msg, subCode, subMsg);
            }
        } catch (Exception e) {
            vo.setErrorMsg("预下单异常: " + e.getMessage());
            log.error("支付宝预下单异常，请检查 application.yml 的 alipay 凭证配置：{}", e.getMessage());
        }
        return vo;
    }

    @Override
    public boolean queryAndSettle(Integer purchaseId, String outTradeNo, String buyerName) {
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            return false;
        }
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response != null && response.isSuccess()) {
                String status = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
                    return purchaseService.markPaid(purchaseId, buyerName);
                }
            }
        } catch (Exception e) {
            log.warn("支付宝交易查询失败（可能凭证未配置）：{}", e.getMessage());
        }
        return false;
    }
}
