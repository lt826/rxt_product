package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.User;
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
        LoginVO loginVO = userService.login(
                loginDTO.getUserName(),
                loginDTO.getPassword(),
                loginDTO.getRole()
        );
        if (loginVO == null) {
            return Result.error(401, "用户名、密码或角色错误");
        }
        return Result.success("登录成功", loginVO);
    }

    @PostMapping("/user/register")
    public Result<String> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功", null);
        }
        return Result.error(400, "用户名已存在");
    }
}