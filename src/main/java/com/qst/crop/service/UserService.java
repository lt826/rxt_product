package com.qst.crop.service;

import com.qst.crop.entity.User;
import com.qst.crop.model.LoginVO;

public interface UserService {
    LoginVO login(String userName, String password, String role);
    boolean register(User user);

    /**
     * 根据用户名获取用户信息
     */
    User getUserByUserName(String userName);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(User user);
}