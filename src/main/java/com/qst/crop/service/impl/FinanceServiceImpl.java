package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinanceMapper;
import com.qst.crop.dao.FinancingIntentionMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.User;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.RecommendPartnerVO;
import com.qst.crop.service.FinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private UserMapper userMapper;

    @Override
    public List<RecommendPartnerVO> recommendPartners(Integer amount, String item) {
        // AI推荐联合贷款人：根据信誉度排序选择得分最高的农户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "farmer");
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
            // 模拟AI匹配分数：基于信誉值的归一化
            double score = Math.min(1.0, (farmer.getCredit() != null ? farmer.getCredit() : 0) / 100.0);
            vo.setMatchScore(Math.round(score * 100.0) / 100.0);
            result.add(vo);
        }
        return result;
    }

    @Override
    public Finance apply(String userName, FinanceApplyDTO dto) {
        Finance finance = new Finance();
        finance.setBankId(dto.getBankId());
        finance.setOwnName(userName);
        finance.setMoney(dto.getMoney());
        finance.setRate(dto.getRate());
        finance.setRepayment(dto.getRepayment());
        finance.setCombinationName1(dto.getCombinationName1());
        finance.setCombinationPhone1(dto.getCombinationPhone1());
        finance.setCombinationIdnum1(dto.getCombinationIdnum1());
        finance.setCombinationName2(dto.getCombinationName2());
        finance.setCombinationPhone2(dto.getCombinationPhone2());
        finance.setCombinationIdnum2(dto.getCombinationIdnum2());
        finance.setFileInfo(dto.getFileInfo());
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
        log.info("融资申请已提交: userName={}, financeId={}", userName, finance.getFinanceId());
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
}
