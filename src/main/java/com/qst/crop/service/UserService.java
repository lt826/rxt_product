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

    /**
     * 修改密码：校验原密码，加密新密码后更新
     *
     * @param userName     用户名
     * @param oldPassword  原密码（明文）
     * @param newPassword  新密码（明文）
     * @return true-修改成功；false-原密码不正确或用户不存在
     */
    boolean changePassword(String userName, String oldPassword, String newPassword);
}