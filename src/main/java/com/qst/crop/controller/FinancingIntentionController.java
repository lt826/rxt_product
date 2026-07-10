package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.FinancingIntention;
import com.qst.crop.model.FinancingIntentionDTO;
import com.qst.crop.service.FinancingIntentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 融资意向控制器
 */
@RestController
@RequestMapping("/api/v1/financing-intention")
public class FinancingIntentionController {

    @Autowired
    private FinancingIntentionService financingIntentionService;

    /**
     * 提交融资意向
     */
    @PostMapping("/submit")
    public Result<FinancingIntention> submit(Authentication auth, @RequestBody FinancingIntentionDTO dto) {
        String userName = auth.getName();
        FinancingIntention intention = financingIntentionService.submit(userName, dto);
        return Result.success("融资意向已提交", intention);
    }

    /**
     * 获取我的融资意向列表
     */
    @GetMapping("/my")
    public Result<List<FinancingIntention>> getMyIntentions(Authentication auth) {
        String userName = auth.getName();
        List<FinancingIntention> list = financingIntentionService.getMyIntentions(userName);
        return Result.success("查询成功", list);
    }

    /**
     * 获取融资意向详情
     */
    @GetMapping("/{id}")
    public Result<FinancingIntention> getDetail(@PathVariable Integer id) {
        FinancingIntention intention = financingIntentionService.getById(id);
        if (intention == null) {
            return Result.error(404, "融资意向不存在");
        }
        return Result.success("查询成功", intention);
    }

    /**
     * 更新融资意向
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Integer id, @RequestBody FinancingIntentionDTO dto) {
        boolean ok = financingIntentionService.update(id, dto);
        return ok ? Result.success("更新成功", true) : Result.error(404, "融资意向不存在");
    }

    /**
     * 删除融资意向
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean ok = financingIntentionService.delete(id);
        return ok ? Result.success("删除成功", true) : Result.error(404, "融资意向不存在");
    }
}
