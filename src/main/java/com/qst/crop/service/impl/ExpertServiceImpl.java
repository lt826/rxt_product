package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Expert;
import com.qst.crop.entity.User;
import com.qst.crop.service.ExpertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpertServiceImpl implements ExpertService {

    @Autowired
    private ExpertMapper expertMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Expert> listAll() {
        // 获取所有已通过审批的专家用户
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("status", "approved");
        userWrapper.eq("role", "expert");
        List<User> approvedUsers = userMapper.selectList(userWrapper);

        // 提取已通过审批的用户名列表
        List<String> approvedUserNames = approvedUsers.stream()
                .map(User::getUserName)
                .collect(Collectors.toList());

        if (approvedUserNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有专家信息
        QueryWrapper<Expert> expertWrapper = new QueryWrapper<>();
        expertWrapper.in("user_name", approvedUserNames);
        List<Expert> existingExperts = expertMapper.selectList(expertWrapper);

        // 找出没有在 tb_expert 表中的用户
        List<String> existingUserNames = existingExperts.stream()
                .map(Expert::getUserName)
                .collect(Collectors.toList());

        List<Expert> result = new ArrayList<>(existingExperts);

        // 为没有专家信息的用户创建默认记录
        for (User user : approvedUsers) {
            if (!existingUserNames.contains(user.getUserName())) {
                Expert expert = new Expert();
                expert.setUserName(user.getUserName());
                expert.setRealName(user.getRealName());
                expert.setPhone(user.getPhone());
                expert.setProfession("农业技术");
                expert.setPosition("专家");
                expert.setBelong("融销通平台");
                result.add(expert);

                // 自动插入到 tb_expert 表
                try {
                    expertMapper.insert(expert);
                } catch (Exception e) {
                    // 忽略插入失败
                }
            }
        }

        return result;
    }

    @Override
    public boolean createExpert(Expert expert) {
        return expertMapper.insert(expert) > 0;
    }
}