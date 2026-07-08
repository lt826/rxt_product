package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Expert;
import com.qst.crop.entity.User;
import com.qst.crop.model.LoginVO;
import com.qst.crop.security.JwtUtil;
import com.qst.crop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExpertMapper expertMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(String userName, String password, String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        User user = userMapper.selectOne(wrapper);

        log.info("查询到的用户: {}", user);
        if (user == null) {
            log.warn("用户不存在: {}", userName);
            return null;
        }

        if (!user.getPassword().equals(password)) {
            log.warn("密码不匹配");
            return null;
        }

        if (role != null && !role.isEmpty() && !user.getRole().equalsIgnoreCase(role)) {
            log.warn("角色不匹配: 期望 {}, 实际 {}", role, user.getRole());
            return null;
        }

        String token = jwtUtil.generateToken(user.getUserName(), user.getRole());
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setRole(user.getRole());
        loginVO.setNickName(user.getNickName());
        loginVO.setAvatar(user.getAvatar());
        return loginVO;
    }

    @Override
    public boolean register(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        if (userMapper.selectOne(wrapper) != null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getIntegral() == null) user.setIntegral(0);
        if (user.getCredit() == null) user.setCredit(0);

        int result = userMapper.insert(user);
        if (result > 0 && "expert".equalsIgnoreCase(user.getRole())) {
            Expert expert = new Expert();
            expert.setUserName(user.getUserName());
            expert.setRealName(user.getRealName());
            expert.setPhone(user.getPhone());
            expert.setProfession("农业技术");
            expert.setPosition("专家");
            expert.setBelong("融销通平台");
            try {
                expertMapper.insert(expert);
            } catch (Exception e) {
                log.warn("创建专家信息失败（不影响用户注册）: {}", e.getMessage());
            }
        }
        return true;
    }
}