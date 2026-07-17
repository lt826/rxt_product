package com.qst.crop.common;

import com.qst.crop.entity.User;

/**
 * 用户资料完整性校验工具
 */
public final class ProfileCheck {

    private ProfileCheck() {
    }

    /**
     * 判断用户的联系方式是否齐全：手机号和地址均已填写（非空白）。
     * 用于农户发布商品、发货等操作的前置校验。
     *
     * @param user 当前用户
     * @return true-手机号和地址均已填写；false-用户不存在或其中任一项为空
     */
    public static boolean contactComplete(User user) {
        if (user == null) {
            return false;
        }
        boolean phoneOk = user.getPhone() != null && !user.getPhone().trim().isEmpty();
        boolean addressOk = user.getAddress() != null && !user.getAddress().trim().isEmpty();
        return phoneOk && addressOk;
    }
}
