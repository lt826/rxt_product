package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 融资意向表实体类
 */
@Data
@TableName("tb_financing_intention")
public class FinancingIntention {

    /** 意向id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户名 */
    private String userName;

    /** 真实姓名 */
    private String realName;

    /** 地址 */
    private String address;

    /** 融资金额 */
    private Integer amount;

    /** 融资用途 */
    private String application;

    /** 融资农作物 */
    private String item;

    /** 还款日期 */
    private Integer repaymentPeriod;

    /** 种植面积 */
    private Integer area;

    /** 手机号 */
    private String phone;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}