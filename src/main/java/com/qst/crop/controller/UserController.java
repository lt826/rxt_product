package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.model.LoginDTO;
import com.qst.crop.model.LoginVO;
import com.qst.crop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

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
}