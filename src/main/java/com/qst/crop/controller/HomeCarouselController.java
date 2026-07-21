package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.HomeCarousel;
import com.qst.crop.service.HomeCarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页轮播图接口
 * <p>
 * - GET    /list        公开，首页拉取轮播图（按展示顺序）
 * - POST   /            管理员，新增轮播图 { imageUrl, link }
 * - DELETE /{id}        管理员，删除轮播图
 * - PUT    /reorder     管理员，按 id 顺序重排 [id, ...]
 * <p>
 * 权限在 WebSecurityConfig 中按路径配置：/list 为 permitAll，其余需 ADMIN。
 */
@RestController
@RequestMapping("/api/v1/home-carousel")
public class HomeCarouselController {

    @Autowired
    private HomeCarouselService homeCarouselService;

    /** 首页：获取轮播图（公开） */
    @GetMapping("/list")
    public Result<List<HomeCarousel>> list() {
        return Result.success("查询成功", homeCarouselService.list());
    }

    /** 后台：新增轮播图（管理员） */
    @PostMapping
    public Result<Boolean> add(@RequestBody HomeCarousel body) {
        homeCarouselService.add(body);
        return Result.success("新增成功", true);
    }

    /** 后台：删除轮播图（管理员） */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        homeCarouselService.delete(id);
        return Result.success("删除成功", true);
    }

    /** 后台：按 id 顺序重排（管理员） */
    @PutMapping("/reorder")
    public Result<Boolean> reorder(@RequestBody List<Long> ids) {
        homeCarouselService.reorder(ids);
        return Result.success("排序成功", true);
    }
}
