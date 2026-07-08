package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.User;
import com.qst.crop.model.LoginVO;
import com.qst.crop.security.JwtUtil;
import com.qst.crop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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

        // 校验密码
        if (!user.getPassword().equals(password)) {
            log.warn("密码不匹配");
            return null;
        }

        // 校验角色是否匹配（若前端未选择角色，则跳过该验证）
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
}