package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 银行表实体类
 */
@Data
@TableName("tb_bank")
public class Bank {

    /** 银行id */
    @TableId(value = "bank_id", type = IdType.AUTO)
    private Integer bankId;

    /** 银行名 */
    private String bankName;

    /** 银行介绍 */
    private String introduce;

    /** 联系电话 */
    private String bankPhone;

    /** 融资金额 */
    private BigDecimal money;

    /** 年利率 */
    private BigDecimal rate;

    /** 还款期限 */
    private Integer repayment;
}