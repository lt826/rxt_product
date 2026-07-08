package com.qst.crop.model;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private String role;
    private String nickName;
    private String avatar;
}