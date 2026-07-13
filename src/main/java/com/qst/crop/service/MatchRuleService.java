package com.qst.crop.service;

import com.qst.crop.entity.MatchRule;

/**
 * 匹配规则服务接口
 */
public interface MatchRuleService {

    /**
     * 获取匹配规则（id=1 单行；不存在则自动初始化默认权重后返回）
     */
    MatchRule getRule();

    /**
     * 更新匹配规则（校验四项权重之和=100）
     */
    boolean updateRule(MatchRule rule);
}
