package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.ReserveMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Reserve;
import com.qst.crop.entity.User;
import com.qst.crop.service.ReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReserveServiceImpl implements ReserveService {

    @Autowired
    private ReserveMapper reserveMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Reserve apply(Reserve reserve) {
        if ((reserve.getPhone() == null || reserve.getPhone().isEmpty())
                && reserve.getQuestioner() != null) {
            User user = userMapper.selectById(reserve.getQuestioner());
            if (user != null) {
                reserve.setPhone(user.getPhone());
            }
        }
        reserve.setStatus(0);
        reserveMapper.insert(reserve);
        return reserve;
    }

    @Override
    public List<Reserve> listByQuestioner(String questioner) {
        QueryWrapper<Reserve> wrapper = new QueryWrapper<>();
        wrapper.eq("questioner", questioner).orderByDesc("id");
        return reserveMapper.selectList(wrapper);
    }

    @Override
    public List<Reserve> listForExpert(String expertName) {
        QueryWrapper<Reserve> wrapper = new QueryWrapper<>();
        wrapper.eq("expert_name", expertName).orderByDesc("id");
        return reserveMapper.selectList(wrapper);
    }

    @Override
    public Reserve handle(Integer id, String expertName, String answer, Integer status) {
        Reserve r = reserveMapper.selectById(id);
        if (r == null) {
            throw new RuntimeException("预约不存在");
        }
        if (expertName != null && !expertName.equals(r.getExpertName())) {
            throw new RuntimeException("无权处理该预约");
        }
        r.setAnswer(answer);
        r.setStatus(status);
        reserveMapper.updateById(r);
        return r;
    }
}
