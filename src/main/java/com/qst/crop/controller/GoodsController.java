package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Goods;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public Result<List<Goods>> list(
            @RequestParam String type,
            @RequestParam(required = false) String keyword) {
        return Result.success("查询成功", goodsService.listByType(type, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Goods> detail(@PathVariable Integer id) {
        return Result.success("查询成功", goodsService.detail(id));
    }

    @PostMapping("/publish")
    public Result<Goods> publish(@RequestBody Goods goods) {
        goods.setOwnName(SecurityUtil.getCurrentUsername());
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
