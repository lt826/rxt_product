package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 银行用户表实体类
 */
@Data
@TableName("tb_bank_user")
public class BankUser {

    /** 用户名（主键） */
    @TableId("user_name")
    private String userName;

    /** 银行id */
    private Integer bankId;

    /** 密码 */
    private String password;

    /** 姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 角色 */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}