package com.qst.crop.controller;

import com.qst.crop.common.ProfileCheck;
import com.qst.crop.common.Result;
import com.qst.crop.entity.Goods;
import com.qst.crop.entity.User;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.GoodsService;
import com.qst.crop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Goods>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success("查询成功", goodsService.listByType(type, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Goods> detail(@PathVariable Integer id) {
        return Result.success("查询成功", goodsService.detail(id));
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Integer id) {
        return Result.success("查询成功", goodsService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Goods> updateById(@PathVariable Integer id, @RequestBody Goods goods) {
        goods.setGoodsId(id);
        goods.setOwnName(SecurityUtil.getCurrentUsername());
        return Result.success("修改成功", goodsService.update(goods));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Integer id) {
        boolean ok = goodsService.delete(id, SecurityUtil.getCurrentUsername());
        return ok ? Result.success("删除成功", true) : Result.error(404, "信息不存在");
    }

    @PostMapping("/publish")
    public Result<Goods> publish(@RequestBody Goods goods) {
        String username = SecurityUtil.getCurrentUsername();
        // 农户发布商品前，必须已完善手机号和地址，方便买家联系与发货
        User user = userService.getUserByUserName(username);
        if (user != null && "farmer".equalsIgnoreCase(user.getRole())
                && !ProfileCheck.contactComplete(user)) {
            return Result.error(400, "请先在「个人中心-基本信息」中填写手机号和地址后再发布商品");
        }
        goods.setOwnName(username);
        return Result.success("发布成功", goodsService.publish(goods));
    }

    @PutMapping("/update")
    public Result<Goods> update(@RequestBody Goods goods) {
        goods.setOwnName(SecurityUtil.getCurrentUsername());
        return Result.success("修改成功", goodsService.update(goods));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean ok = goodsService.delete(id, SecurityUtil.getCurrentUsername());
        return ok ? Result.success("删除成功", true) : Result.error(404, "信息不存在");
    }

    @GetMapping("/my")
    public Result<List<Goods>> myGoods(@RequestParam(required = false) String type) {
        return Result.success("查询成功",
                goodsService.listByOwner(SecurityUtil.getCurrentUsername(), type));
    }
}
