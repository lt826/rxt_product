package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.User;
import com.qst.crop.exception.LoginException;
import com.qst.crop.model.ChangePasswordDTO;
import com.qst.crop.model.LoginDTO;
import com.qst.crop.model.LoginVO;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.ExpertService;
import com.qst.crop.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpertService expertService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/user/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        try {
            LoginVO loginVO = userService.login(
                    loginDTO.getUserName(),
                    loginDTO.getPassword(),
                    loginDTO.getRole()
            );
            if (loginVO == null) {
                return Result.error(401, "用户名、密码或角色错误");
            }
            return Result.success("登录成功", loginVO);
        } catch (LoginException e) {
            return Result.error(e.getStatusCode(), e.getMessage());
        }
    }

    @PostMapping("/user/register")
    public Result<String> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功", null);
        }
        return Result.error(400, "用户名已存在");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public Result<User> getUserInfo(@RequestParam String userName) {
        User user = userService.getUserByUserName(userName);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        // 清除敏感信息
        user.setPassword(null);
        return Result.success("查询成功", user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user/update")
    public Result<String> updateUserInfo(@RequestBody User user) {
        boolean success = userService.updateUserInfo(user);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error(400, "更新失败");
    }

    /**
     * 修改密码：以 JWT 中的当前登录用户为准，仅可修改自己的密码
     */
    @PutMapping("/user/password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto) {
        String currentUserName = SecurityUtil.getCurrentUsername();
        if (currentUserName == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return Result.error(400, "新密码长度不少于6位");
        }
        boolean success = userService.changePassword(
                currentUserName, dto.getOldPassword(), dto.getNewPassword());
        if (success) {
            return Result.success("密码修改成功", null);
        }
        return Result.error(400, "原密码不正确");
    }

    /** 获取所有农户列表（用于买家联系） */
    @GetMapping("/farmers")
    public Result<List<User>> farmers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "farmer");
        return Result.success("查询成功", userMapper.selectList(wrapper));
    }
}