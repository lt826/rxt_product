package com.qst.crop.model;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordDTO {
    /** 原密码 */
    private String oldPassword;
    /** 新密码 */
    private String newPassword;
}
