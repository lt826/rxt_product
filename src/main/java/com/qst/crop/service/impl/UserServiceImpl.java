package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.ExpertMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Expert;
import com.qst.crop.entity.User;
import com.qst.crop.exception.LoginException;
import com.qst.crop.model.LoginVO;
import com.qst.crop.security.JwtUtil;
import com.qst.crop.security.SecurityUtil;
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

        // 使用加密后的密码验证
        if (!SecurityUtil.matchesPassword(password, user.getPassword())) {
            log.warn("密码不匹配");
            return null;
        }

        // 检查用户状态
        if ("rejected".equals(user.getStatus())) {
            log.warn("用户已被拒绝: {}", userName);
            throw new LoginException("您的账号申请已被拒绝，请联系管理员");
        }
        if ("pending".equals(user.getStatus())) {
            log.warn("用户待审批: {}", userName);
            throw new LoginException("您的账号正在等待管理员审批，请耐心等待");
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

        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecurityUtil.encryptPassword(user.getPassword()));
        }

        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getIntegral() == null) user.setIntegral(0);
        if (user.getCredit() == null) user.setCredit(0);

        // 根据角色设置状态：bank、expert、admin 需要审批
        if ("bank".equalsIgnoreCase(user.getRole())
                || "expert".equalsIgnoreCase(user.getRole())
                || "admin".equalsIgnoreCase(user.getRole())) {
            user.setStatus("pending");
            log.info("用户 {} 注册角色 {}，需要管理员审批", user.getUserName(), user.getRole());
        } else {
            user.setStatus("approved");
        }

        int result = userMapper.insert(user);
        // 专家信息在审批通过后创建
        return true;
    }

    @Override
    public User getUserByUserName(String userName) {
        return userMapper.selectById(userName);
    }

    @Override
    public boolean updateUserInfo(User user) {
        User existingUser = userMapper.selectById(user.getUserName());
        if (existingUser == null) {
            return false;
        }

        // 只更新允许修改的字段
        User updateUser = new User();
        updateUser.setUserName(user.getUserName());
        updateUser.setUpdateTime(LocalDateTime.now());

        // 可修改的字段
        if (user.getNickName() != null) {
            updateUser.setNickName(user.getNickName());
        }
        if (user.getPhone() != null) {
            updateUser.setPhone(user.getPhone());
        }
        if (user.getAddress() != null) {
            updateUser.setAddress(user.getAddress());
        }
        if (user.getRealName() != null) {
            updateUser.setRealName(user.getRealName());
        }
        if (user.getIdentityNum() != null) {
            updateUser.setIdentityNum(user.getIdentityNum());
        }
        if (user.getAvatar() != null) {
            updateUser.setAvatar(user.getAvatar());
        }

        int result = userMapper.updateById(updateUser);
        return result > 0;
    }
}