package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.model.FeatureItemDTO;
import com.qst.crop.model.HomepageFeatureVO;
import com.qst.crop.model.SectionConfigVO;
import com.qst.crop.service.HomepageFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页精选内容接口
 * <p>
 * - GET  /list                公开，首页拉取各板块已精选并解析好的内容
 * - GET  /section?section=xxx 管理员，获取某板块候选内容与当前已选
 * - PUT  /section/{section}   管理员，整体保存某板块的精选内容
 * <p>
 * 权限在 WebSecurityConfig 中按路径配置：/list 为 permitAll，/section/** 需 ADMIN。
 */
@RestController
@RequestMapping("/api/v1/homepage-feature")
public class HomepageFeatureController {

    @Autowired
    private HomepageFeatureService homepageFeatureService;

    /** 首页：获取各板块精选内容（公开） */
    @GetMapping("/list")
    public Result<HomepageFeatureVO> list() {
        return Result.success("查询成功", homepageFeatureService.resolveForHome());
    }

    /** 后台：获取某板块候选内容与已选配置（管理员） */
    @GetMapping("/section")
    public Result<SectionConfigVO> section(@RequestParam String section) {
        return Result.success("查询成功", homepageFeatureService.getSectionConfig(section));
    }

    /** 后台：整体保存某板块的精选内容（管理员） */
    @PutMapping("/section/{section}")
    public Result<Boolean> saveSection(@PathVariable String section,
                                       @RequestBody(required = false) List<FeatureItemDTO> items) {
        homepageFeatureService.saveSection(section, items);
        return Result.success("保存成功", true);
    }
}
