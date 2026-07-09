package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@TableName("tb_user")
public class User {

    /** 用户名（主键） */
    @TableId("user_name")
    private String userName;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String nickName;

    /** 手机号 */
    private String phone;

    /** 身份证号 */
    private String identityNum;

    /** 地址 */
    private String address;

    /** 角色 */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 积分 */
    private Integer integral;

    /** 信誉 */
    private Integer credit;

    /** 头像 */
    private String avatar;

    /** 真实姓名 */
    private String realName;

    /** 状态：pending-待审批，approved-已通过，rejected-已拒绝 */
    private String status;
}