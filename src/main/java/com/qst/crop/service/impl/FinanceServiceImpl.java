package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinanceMapper;
import com.qst.crop.dao.FinancingIntentionMapper;
import com.qst.crop.dao.FinancingProductMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.FinancingIntention;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.entity.User;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.RecommendPartnerVO;
import com.qst.crop.service.FinanceService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        return true;
    }
}