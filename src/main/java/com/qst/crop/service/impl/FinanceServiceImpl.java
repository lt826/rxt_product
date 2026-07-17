package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinanceMapper;
import com.qst.crop.dao.FinancingIntentionMapper;
import com.qst.crop.dao.FinancingProductMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.FinancingIntention;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.entity.MatchRule;
import com.qst.crop.entity.User;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.FinanceApprovalVO;
import com.qst.crop.model.RecommendPartnerVO;
import com.qst.crop.service.FinanceService;
import com.qst.crop.service.MatchRuleService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private FinancingIntentionMapper financingIntentionMapper;

    @Autowired
    private FinancingProductMapper financingProductMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MatchRuleService matchRuleService;

    // 最长还款期限（月），用于计算还款期限权重
    private static final int MAX_REPAYMENT_PERIOD = 36;

    @Override
    public List<RecommendPartnerVO> recommendPartners(String currentUserName, Integer amount, String item) {
        // AI推荐联合贷款人：排除自己，根据信誉度排序选择得分最高的农户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "farmer");
        wrapper.ne("user_name", currentUserName); // 排除自己
        wrapper.orderByDesc("credit");
        wrapper.last("LIMIT 10");
        List<User> farmers = userMapper.selectList(wrapper);

        List<RecommendPartnerVO> result = new ArrayList<>();
        for (User farmer : farmers) {
            RecommendPartnerVO vo = new RecommendPartnerVO();
            vo.setUserName(farmer.getUserName());
            vo.setRealName(farmer.getRealName());
            vo.setPhone(farmer.getPhone());
            vo.setCredit(farmer.getCredit());
            vo.setAddress(farmer.getAddress()); // 设置地址

            // 获取该农户最新的融资意向
            QueryWrapper<FinancingIntention> intentionWrapper = new QueryWrapper<>();
            intentionWrapper.eq("user_name", farmer.getUserName());
            intentionWrapper.orderByDesc("create_time");
            intentionWrapper.last("LIMIT 1");
            FinancingIntention intention = financingIntentionMapper.selectOne(intentionWrapper);

            double matchScore = 0.0;
            int credit = farmer.getCredit() != null ? farmer.getCredit() : 0;

            if (intention == null) {
                // 如果没有融资意向，只算信誉分
                matchScore = credit;
                vo.setCropItem("未设置"); // 设置默认作物
                log.debug("农户 {} 无融资意向，匹配度仅基于信誉分: {}", farmer.getUserName(), matchScore);
            } else {
                vo.setCropItem(intention.getItem() != null ? intention.getItem() : "未设置"); // 设置种植作物
                // 有融资意向，按权重计算匹配度（满分100分）
                // 1. 信誉分占60%
                double creditScore = credit * 0.6;

                // 2. 融资农作物占20% - 如果与当前农户申请的农作物相同则满分
                double cropScore = 0.0;
                if (item != null && item.equals(intention.getItem())) {
                    cropScore = 20.0; // 相同农作物，得满分20%
                }
                log.debug("农户 {} 作物匹配: 申请作物={}, 意向作物={}, 作物得分={}",
                    farmer.getUserName(), item, intention.getItem(), cropScore);

                // 3. 还款期限占20% - 期限越短占比越大
                int repaymentPeriod = intention.getRepaymentPeriod() != null ? intention.getRepaymentPeriod() : 12;
                double periodScore = 0.0;
                if (repaymentPeriod > 0) {
                    // 使用线性递减公式：期限越短，分数越高
                    // 最短期限6个月得满分20分，最长期限36个月得0分
                    int maxPeriod = MAX_REPAYMENT_PERIOD;
                    int minPeriod = 6;
                    if (repaymentPeriod <= minPeriod) {
                        periodScore = 20.0; // 最短期限，满分20分
                    } else if (repaymentPeriod >= maxPeriod) {
                        periodScore = 0.0; // 最长期限，0分
                    } else {
                        // 线性插值：(maxPeriod - period) / (maxPeriod - minPeriod) * 20
                        periodScore = ((double)(maxPeriod - repaymentPeriod) / (maxPeriod - minPeriod)) * 20.0;
                    }
                }
                log.debug("农户 {} 还款期限匹配: 期限={}, 期限得分={}",
                    farmer.getUserName(), repaymentPeriod, periodScore);

                matchScore = creditScore + cropScore + periodScore;
                log.debug("农户 {} 总匹配度: 信誉分{} + 作物{} + 期限{} = {}",
                    farmer.getUserName(), creditScore, cropScore, periodScore, matchScore);
            }

            vo.setMatchScore(Math.round(matchScore * 1000.0) / 1000.0); // 保留3位小数
            result.add(vo);
        }

        // 按匹配度降序排序
        result.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return result;
    }

    @Override
    public Finance apply(String userName, FinanceApplyDTO dto) {
        Finance finance = new Finance();

        // 如果指定了产品ID，从产品中获取银行ID和利率
        if (dto.getProductId() != null) {
            FinancingProduct product = financingProductMapper.selectById(dto.getProductId());
            if (product != null) {
                finance.setBankId(product.getBankId());
                finance.setRate(product.getRate());
            } else {
                log.warn("产品不存在: productId={}", dto.getProductId());
            }
        } else if (dto.getBankId() != null) {
            finance.setBankId(dto.getBankId());
        }

        finance.setOwnName(userName);
        finance.setMoney(dto.getMoney());

        // 如果DTO中指定了利率，使用DTO的利率（覆盖产品利率）
        if (dto.getRate() != null) {
            finance.setRate(dto.getRate());
        }

        finance.setRepayment(dto.getRepayment());
        finance.setCombinationName1(dto.getCombinationName1());
        finance.setCombinationPhone1(dto.getCombinationPhone1());
        finance.setCombinationName2(dto.getCombinationName2());
        finance.setCombinationPhone2(dto.getCombinationPhone2());
        finance.setFileInfo(dto.getFileInfo());
        finance.setCropItem(dto.getCropItem()); // 设置融资农作物
        finance.setStatus(0); // 申请中
        LocalDateTime now = LocalDateTime.now();
        finance.setCreateTime(now);
        finance.setUpdateTime(now);

        // 从用户表获取真实姓名和电话
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user_name", userName);
        User user = userMapper.selectOne(userWrapper);
        if (user != null) {
            finance.setRealName(user.getRealName());
            finance.setPhone(user.getPhone());
            finance.setIdNum(user.getIdentityNum());
        }

        financeMapper.insert(finance);
        log.info("融资申请已提交: userName={}, financeId={}, productId={}",
            userName, finance.getFinanceId(), dto.getProductId());
        // 通知所有银行端有新的融资审批待处理
        try {
            String applicant = finance.getRealName() != null ? finance.getRealName() : userName;
            BigDecimal money = finance.getMoney() != null ? finance.getMoney() : BigDecimal.ZERO;
            String content = String.format("用户「%s」提交了一笔融资金额 ¥%,.2f 的申请，请前往审批。", applicant, money);
            notificationService.sendToRole("bank", "融资审批待处理", content, "finance_apply", finance.getFinanceId());
        } catch (Exception e) {
            log.warn("融资申请通知发送失败: {}", e.getMessage());
        }
        return finance;
    }

    @Override
    public List<Finance> getMyFinances(String userName) {
        QueryWrapper<Finance> wrapper = new QueryWrapper<>();
        wrapper.eq("own_name", userName);
        wrapper.orderByDesc("create_time");
        return financeMapper.selectList(wrapper);
    }

    @Override
    public Finance getFinanceById(Integer financeId) {
        return financeMapper.selectById(financeId);
    }
    

    @Override
    public List<Finance> getAllFinances() {
        QueryWrapper<Finance> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return financeMapper.selectList(wrapper);
    }

    @Override
    public List<FinanceApprovalVO> getApprovalList() {
        List<Finance> finances = getAllFinances();
        List<FinanceApprovalVO> list = new ArrayList<>();
        if (finances == null || finances.isEmpty()) {
            return list;
        }

        // 批量查询申请人信誉分，避免 N+1
        Set<String> userNames = new HashSet<>();
        for (Finance f : finances) {
            if (isNotBlank(f.getOwnName())) {
                userNames.add(f.getOwnName());
            }
        }
        Map<String, Integer> creditMap = new HashMap<>();
        if (!userNames.isEmpty()) {
            QueryWrapper<User> uw = new QueryWrapper<>();
            uw.in("user_name", userNames);
            List<User> users = userMapper.selectList(uw);
            for (User u : users) {
                creditMap.put(u.getUserName(), u.getCredit() != null ? u.getCredit() : 0);
            }
        }

        MatchRule rule = matchRuleService.getRule();
        for (Finance f : finances) {
            FinanceApprovalVO vo = new FinanceApprovalVO();
            vo.setFinanceId(f.getFinanceId());
            vo.setRealName(f.getRealName());
            vo.setOwnName(f.getOwnName());
            vo.setMoney(f.getMoney());
            vo.setRate(f.getRate());
            vo.setRepayment(f.getRepayment());
            vo.setStatus(f.getStatus());
            vo.setRemark(f.getRemark());
            vo.setCreateTime(f.getCreateTime());

            // 联合贷款人姓名拼接与计数
            List<String> coNames = new ArrayList<>();
            if (isNotBlank(f.getCombinationName1())) coNames.add(f.getCombinationName1());
            if (isNotBlank(f.getCombinationName2())) coNames.add(f.getCombinationName2());
            vo.setCoLenderCount(coNames.size());
            vo.setCoLenderNames(coNames.isEmpty() ? "—" : String.join("、", coNames));

            Integer credit = creditMap.getOrDefault(f.getOwnName(), 0);
            vo.setCredit(credit);
            vo.setMatchScore(computeMatchScore(f, credit, rule));

            list.add(vo);
        }
        return list;
    }

    /**
     * 计算单笔融资申请的匹配度（0~100）：
     * 联合子分×Wp + 信誉子分×Wc + 金额子分×Wa + 期限子分×Wt（四项权重之和=100）
     */
    private double computeMatchScore(Finance f, Integer credit, MatchRule rule) {
        // 1. 联合贷款人子分：min(人数,2)/2 × 100（0人=0，1人=50，2人=100）
        int count = 0;
        if (isNotBlank(f.getCombinationName1())) count++;
        if (isNotBlank(f.getCombinationName2())) count++;
        double partnerSub = Math.min(count, 2) / 2.0 * 100.0;

        // 2. 信誉子分：clamp(credit,0,100)
        int c = credit != null ? credit : 0;
        c = Math.max(0, Math.min(100, c));
        double creditSub = c;

        // 3. 金额子分：金额越低越高 (1 - min(money/上限,1)) × 100
        BigDecimal upper = rule.getAmountUpperBound();
        double upperVal = (upper != null && upper.doubleValue() > 0) ? upper.doubleValue() : 500000.0;
        double money = f.getMoney() != null ? f.getMoney().doubleValue() : 0.0;
        double amountRatio = Math.max(0.0, Math.min(money / upperVal, 1.0));
        double amountSub = (1.0 - amountRatio) * 100.0;

        // 4. 期限子分：越短越高 (1 - clamp((term-6)/(36-6),0,1)) × 100
        int term = f.getRepayment() != null ? f.getRepayment() : 12;
        int minP = 6;
        int maxP = MAX_REPAYMENT_PERIOD; // 36
        double termRatio;
        if (term <= minP) {
            termRatio = 0.0;
        } else if (term >= maxP) {
            termRatio = 1.0;
        } else {
            termRatio = ((double) (term - minP)) / ((double) (maxP - minP));
        }
        double termSub = (1.0 - termRatio) * 100.0;

        double wp = num(rule.getPartnerWeight());
        double wc = num(rule.getCreditWeight());
        double wa = num(rule.getAmountWeight());
        double wt = num(rule.getTermWeight());

        double score = partnerSub * wp / 100.0
                + creditSub * wc / 100.0
                + amountSub * wa / 100.0
                + termSub * wt / 100.0;
        return Math.round(score * 10.0) / 10.0; // 保留1位小数
    }

    private double num(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    @Override
    public boolean updateFinanceStatus(Integer financeId, Integer status, String remark) {
        Finance finance = financeMapper.selectById(financeId);
        if (finance == null) return false;
        finance.setStatus(status);
        finance.setRemark(remark);
        finance.setUpdateTime(LocalDateTime.now());
        financeMapper.updateById(finance);

        // 发送通知给申请人
        String title;
        String content;
        String type;

        if (status == 1) {
            // 申请通过
            title = "融资申请已通过";
            content = String.format("您的融资申请已通过审核！%n申请编号：%d%n融资金额：¥%,.2f%n还款期限：%d个月%n审批时间：%s",
                finance.getFinanceId(),
                finance.getMoney() != null ? finance.getMoney() : BigDecimal.ZERO,
                finance.getRepayment() != null ? finance.getRepayment() : 0,
                finance.getUpdateTime().toString());
            type = "approve";
        } else if (status == 2) {
            // 申请驳回
            title = "融资申请已驳回";
            content = String.format("您的融资申请已被驳回。%n申请编号：%d%n融资金额：¥%,.2f%n驳回原因：%s%n审批时间：%s",
                finance.getFinanceId(),
                finance.getMoney() != null ? finance.getMoney() : BigDecimal.ZERO,
                remark != null ? remark : "未填写",
                finance.getUpdateTime().toString());
            type = "reject";
        } else {
            return true;
        }

        notificationService.send(finance.getOwnName(), title, content, type, financeId);
        log.info("融资申请状态已更新并发送通知: financeId={}, status={}, receiver={}",
            financeId, status, finance.getOwnName());

        // 同时把审批结果通知联合贷款人（按电话查找其用户账号后发送站内通知）
        notifyCoLenders(finance, status == 1, remark);

        return true;
    }

    /**
     * 通知该笔申请的联合贷款人（通过 combination_phone 查找其用户账号）
     */
    private void notifyCoLenders(Finance finance, boolean approved, String remark) {
        String title = approved ? "您参与的联合融资申请已通过" : "您参与的联合融资申请已驳回";
        String result = approved ? "已通过银行审核" : "已被银行驳回";
        String tail = approved ? "" : ("驳回原因：" + (remark != null && !remark.trim().isEmpty() ? remark : "未填写"));
        String content = String.format(
                "您作为联合贷款人参与的融资申请（编号：%d，主申请人：%s）%s。%s",
                finance.getFinanceId(),
                finance.getRealName() != null ? finance.getRealName() : finance.getOwnName(),
                result,
                tail);
        String type = approved ? "approve" : "reject";
        notifyCoLenderByPhone(finance.getCombinationPhone1(), title, content, type, finance.getFinanceId(), finance.getOwnName());
        notifyCoLenderByPhone(finance.getCombinationPhone2(), title, content, type, finance.getFinanceId(), finance.getOwnName());
    }

    private void notifyCoLenderByPhone(String phone, String title, String content, String type, Integer financeId, String excludeUserName) {
        if (phone == null || phone.trim().isEmpty()) {
            return;
        }
        QueryWrapper<User> uw = new QueryWrapper<>();
        uw.eq("phone", phone);
        uw.last("LIMIT 1");
        User u = userMapper.selectOne(uw);
        if (u == null) {
            // 联合贷款人非系统注册用户，无法发送站内通知
            return;
        }
        if (excludeUserName != null && excludeUserName.equals(u.getUserName())) {
            // 与主申请人同一账号，避免重复通知
            return;
        }
        notificationService.send(u.getUserName(), title, content, type, financeId);
        log.info("联合贷款人通知已发送: financeId={}, coLender={}", financeId, u.getUserName());
    }
}