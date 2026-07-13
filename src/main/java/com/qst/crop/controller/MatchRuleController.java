package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.MatchRule;
import com.qst.crop.service.MatchRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 匹配规则控制器（银行端调整匹配度各因子权重）
 */
@RestController
@RequestMapping("/api/v1/match-rule")
public class MatchRuleController {

    @Autowired
    private MatchRuleService matchRuleService;

    /** 获取当前匹配规则权重 */
    @GetMapping
    public Result<MatchRule> getRule() {
        return Result.success("查询成功", matchRuleService.getRule());
    }

    /** 保存匹配规则权重（四项权重之和必须=100） */
    @PutMapping
    public Result<Boolean> updateRule(@RequestBody MatchRule rule) {
        boolean ok = matchRuleService.updateRule(rule);
        return ok ? Result.success("保存成功", true) : Result.error(400, "四项权重之和必须等于100");
    }
}
