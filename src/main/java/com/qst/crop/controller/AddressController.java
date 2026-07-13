package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Address;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货/发货地址管理：以 JWT 中的当前登录用户为准，仅能操作自己的地址
 */
@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /** 查询当前用户的全部地址 */
    @GetMapping("/list")
    public Result<List<Address>> list() {
        String current = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", addressService.listByUser(current));
    }

    /** 新增地址 */
    @PostMapping
    public Result<Address> add(@RequestBody Address address) {
        String current = SecurityUtil.getCurrentUsername();
        if (current == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        address.setId(null);
        address.setOwnName(current);
        Address saved = addressService.add(address);
        return Result.success("添加成功", saved);
    }

    /** 更新地址 */
    @PutMapping
    public Result<String> update(@RequestBody Address address) {
        String current = SecurityUtil.getCurrentUsername();
        if (current == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        Address existing = address.getId() == null ? null : addressService.listByUser(current).stream()
                .filter(a -> a.getId().equals(address.getId())).findFirst().orElse(null);
        if (existing == null) {
            return Result.error(404, "地址不存在或无权操作");
        }
        boolean ok = addressService.update(address);
        return ok ? Result.success("更新成功", null) : Result.error(400, "更新失败");
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        String current = SecurityUtil.getCurrentUsername();
        if (current == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        boolean ok = addressService.delete(id, current);
        return ok ? Result.success("删除成功", null) : Result.error(400, "删除失败");
    }
}
