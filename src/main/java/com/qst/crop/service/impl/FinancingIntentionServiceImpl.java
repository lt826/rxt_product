package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinancingIntentionMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.FinancingIntention;
import com.qst.crop.entity.User;
import com.qst.crop.model.FinancingIntentionDTO;
import com.qst.crop.service.FinancingIntentionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 融资意向服务实现类
 */
@Slf4j
@Service
public class FinancingIntentionServiceImpl implements FinancingIntentionService {

    @Autowired
    private FinancingIntentionMapper financingIntentionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public FinancingIntention submit(String userName, FinancingIntentionDTO dto) {
        FinancingIntention intention = new FinancingIntention();
        intention.setUserName(userName);
        intention.setAmount(dto.getAmount() != null ? dto.getAmount() : 0);
        intention.setApplication(dto.getApplication() != null ? dto.getApplication() : "");
        intention.setItem(dto.getItem() != null ? dto.getItem() : "");
        intention.setRepaymentPeriod(dto.getRepaymentPeriod() != null ? dto.getRepaymentPeriod() : 12); // 默认12个月
        intention.setArea(dto.getArea() != null ? dto.getArea() : 0); // 默认0亩

        // 从用户表获取真实姓名、地址和电话
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user_name", userName);
        User user = userMapper.selectOne(userWrapper);
        if (user != null) {
            intention.setRealName(user.getRealName() != null ? user.getRealName() : "");
            intention.setAddress(user.getAddress() != null ? user.getAddress() : "");
            intention.setPhone(user.getPhone() != null ? user.getPhone() : "");
        } else {
            // 设置默认空值
            intention.setRealName("");
            intention.setAddress("");
            intention.setPhone("");
        }

        LocalDateTime now = LocalDateTime.now();
        intention.setCreateTime(now);
        intention.setUpdateTime(now);

        financingIntentionMapper.insert(intention);
        log.info("融资意向已提交: userName={}, id={}", userName, intention.getId());
        return intention;
    }

    @Override
    public List<FinancingIntention> getMyIntentions(String userName) {
        QueryWrapper<FinancingIntention> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        wrapper.orderByDesc("create_time");
        return financingIntentionMapper.selectList(wrapper);
    }

    @Override
    public FinancingIntention getById(Integer id) {
        return financingIntentionMapper.selectById(id);
    }

    @Override
    public boolean update(Integer id, FinancingIntentionDTO dto) {
        FinancingIntention intention = financingIntentionMapper.selectById(id);
        if (intention == null) {
            return false;
        }
        intention.setAmount(dto.getAmount());
        intention.setApplication(dto.getApplication());
        intention.setItem(dto.getItem());
        intention.setRepaymentPeriod(dto.getRepaymentPeriod());
        intention.setArea(dto.getArea());
        intention.setUpdateTime(LocalDateTime.now());
        financingIntentionMapper.updateById(intention);
        return true;
    }

    @Override
    public boolean delete(Integer id) {
        int rows = financingIntentionMapper.deleteById(id);
        return rows > 0;
    }
}
