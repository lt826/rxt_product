package com.qst.crop.service.impl;

import com.qst.crop.dao.MatchRuleMapper;
import com.qst.crop.entity.MatchRule;
import com.qst.crop.service.MatchRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配规则服务实现类
 */
@Slf4j
@Service
public class MatchRuleServiceImpl implements MatchRuleService {

    @Autowired
    private MatchRuleMapper matchRuleMapper;

    @Override
    public MatchRule getRule() {
        MatchRule rule = matchRuleMapper.selectById(1);
        if (rule == null) {
            // 自愈：不存在则插入默认权重行（schema.sql 不会被 initializer 执行 INSERT，故在此兜底）
            rule = new MatchRule();
            rule.setId(1);
            rule.setPartnerWeight(new BigDecimal("20"));
            rule.setCreditWeight(new BigDecimal("30"));
            rule.setAmountWeight(new BigDecimal("25"));
            rule.setTermWeight(new BigDecimal("25"));
            rule.setAmountUpperBound(new BigDecimal("500000"));
            rule.setUpdateTime(LocalDateTime.now());
            matchRuleMapper.insert(rule);
            log.info("匹配规则默认权重行已初始化: 联合20/信誉30/金额25/期限25");
        }
        return rule;
    }

    @Override
    public boolean updateRule(MatchRule rule) {
        double sum = sumWeights(rule);
        if (Math.abs(sum - 100.0) > 0.01) {
            log.warn("匹配规则权重之和不为100，拒绝保存: sum={}", sum);
            return false;
        }
        rule.setId(1);
        if (rule.getAmountUpperBound() == null
                || rule.getAmountUpperBound().doubleValue() <= 0) {
            rule.setAmountUpperBound(new BigDecimal("500000"));
        }
        rule.setUpdateTime(LocalDateTime.now());
        int rows = matchRuleMapper.updateById(rule);
        if (rows == 0) {
            // 行尚未初始化（例如未调用过 getRule），则插入
            matchRuleMapper.insert(rule);
        }
        log.info("匹配规则已更新: 联合{}/信誉{}/金额{}/期限{}",
                rule.getPartnerWeight(), rule.getCreditWeight(),
                rule.getAmountWeight(), rule.getTermWeight());
        return true;
    }

    private double sumWeights(MatchRule r) {
        double s = 0;
        if (r.getPartnerWeight() != null) s += r.getPartnerWeight().doubleValue();
        if (r.getCreditWeight() != null) s += r.getCreditWeight().doubleValue();
        if (r.getAmountWeight() != null) s += r.getAmountWeight().doubleValue();
        if (r.getTermWeight() != null) s += r.getTermWeight().doubleValue();
        return s;
    }
}
