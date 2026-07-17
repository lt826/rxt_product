package com.qst.crop.controller;

import com.qst.crop.common.ProfileCheck;
import com.qst.crop.common.Result;
import com.qst.crop.entity.User;
import com.qst.crop.model.CreateOrderDTO;
import com.qst.crop.model.OrderVO;
import com.qst.crop.model.ShipDTO;
import com.qst.crop.service.PurchaseService;
import com.qst.crop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 买家下单 / 我买到的 / 我卖出的 / 农户发货 / 买家确认收货
 */
@RestController
@RequestMapping("/api/v1/orders")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserService userService;

    /** 买家下单 */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody CreateOrderDTO dto, Authentication auth) {
        Integer id = purchaseService.createOrder(auth.getName(), dto);
        if (id == null) {
            return Result.error(400, "下单失败：请检查收货地址和商品");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("purchaseId", id);
        return Result.success("下单成功", data);
    }

    /** 买家"我买到的"订单 */
    @GetMapping("/bought")
    public Result<List<OrderVO>> bought(Authentication auth) {
        return Result.success("获取成功", purchaseService.listBought(auth.getName()));
    }

    /** 卖家"我卖出的"订单 */
    @GetMapping("/sold")
    public Result<List<OrderVO>> sold(Authentication auth) {
        return Result.success("获取成功", purchaseService.listSold(auth.getName()));
    }

    /** 农户发货：填写物流信息并把状态改为待收货 */
    @PutMapping("/{id}/ship")
    public Result<Boolean> ship(@PathVariable Integer id, @RequestBody ShipDTO dto, Authentication auth) {
        // 发货前，农户必须已完善手机号和地址，方便买家联系与物流配送
        User user = userService.getUserByUserName(auth.getName());
        if (!ProfileCheck.contactComplete(user)) {
            return Result.error(400, "请先在「个人中心-基本信息」中填写手机号和地址后再发货");
        }
        boolean ok = purchaseService.ship(id, auth.getName(), dto);
        return ok ? Result.success("发货成功", true) : Result.error(404, "订单不存在或无权操作");
    }

    /** 买家确认收货：状态改为已收货 */
    @PutMapping("/{id}/receive")
    public Result<Boolean> receive(@PathVariable Integer id, Authentication auth) {
        boolean ok = purchaseService.receive(id, auth.getName());
        return ok ? Result.success("确认收货成功", true) : Result.error(404, "订单不存在或无权操作");
    }
}
