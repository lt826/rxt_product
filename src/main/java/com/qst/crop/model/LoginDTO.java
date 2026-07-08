package com.qst.crop.model;

import lombok.Data;

@Data
public class LoginDTO {
    private String userName;
    private String password;
    private String role;   // 新增：用户选择的角色
}