package com.qst.crop.service;

import com.qst.crop.entity.User;
import com.qst.crop.model.LoginVO;

public interface UserService {
    LoginVO login(String userName, String password, String role);
    boolean register(User user);
}