package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.User;
import com.qst.crop.exception.LoginException;
import com.qst.crop.model.LoginDTO;
import com.qst.crop.model.LoginVO;
import com.qst.crop.service.ExpertService;
import com.qst.crop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpertService expertService;

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
}