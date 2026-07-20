package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.model.PayQrcodeVO;
import com.qst.crop.service.PayService;
import com.qst.crop.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付接口
 * <p>
 * - GET  /qrcode?purchaseId=        生成扫码支付二维码（预下单）
 * - GET  /query?purchaseId=&outTradeNo=  轮询查询支付状态（扫码后自动检测）
 * - POST /confirm/{purchaseId}       手动「已完成支付」（demo 友好，不依赖真实支付）
 * <p>
 * 权限：落到 WebSecurityConfig 的 anyRequest().authenticated()，service 内校验订单归属。
 */
@RestController
@RequestMapping("/api/v1/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private PurchaseService purchaseService;

    /** 生成支付宝扫码支付二维码 */
    @GetMapping("/qrcode")
    public Result<PayQrcodeVO> qrcode(@RequestParam Integer purchaseId, Authentication auth) {
        return Result.success("查询成功", payService.createQrcode(purchaseId, auth.getName()));
    }

    /** 查询支付状态（前端轮询）；已支付则结算订单 */
    @GetMapping("/query")
    public Result<Map<String, Object>> query(@RequestParam Integer purchaseId,
                                             @RequestParam(required = false) String outTradeNo,
                                             Authentication auth) {
        boolean paid = payService.queryAndSettle(purchaseId, outTradeNo, auth.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("paid", paid);
        return Result.success("查询成功", data);
    }

    /** 手动「已完成支付」：直接结算订单（无需真实支付） */
    @PostMapping("/confirm/{purchaseId}")
    public Result<Boolean> confirm(@PathVariable Integer purchaseId, Authentication auth) {
        boolean ok = purchaseService.markPaid(purchaseId, auth.getName());
        return ok ? Result.success("支付成功", true) : Result.error(404, "订单不存在或无权操作");
    }
}
