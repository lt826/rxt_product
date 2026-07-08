package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 融资表实体类
 */
@Data
@TableName("tb_finance")
public class Finance {

    /** id */
    @TableId(value = "finance_id", type = IdType.AUTO)
    private Integer financeId;

    /** 银行id */
    private Integer bankId;

    /** 用户名 */
    private String ownName;

    /** 真实姓名 */
    private String realName;

    /** 电话 */
    private String phone;

    /** 身份证号 */
    private String idNum;

    /** 状态，0：申请中，1：驳回，2：申请通过 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 融资金额 */
    private BigDecimal money;

    /** 年利率 */
    private BigDecimal rate;

    /** 还款期限 */
    private Integer repayment;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 联合贷款人1姓名 */
    private String combinationName1;

    /** 联合贷款人1电话 */
    private String combinationPhone1;

    /** 联合贷款人1身份证 */
    private String combinationIdnum1;

    /** 联合贷款人2姓名 */
    private String combinationName2;

    /** 联合贷款人2电话 */
    private String combinationPhone2;

    /** 联合贷款人2身份证 */
    private String combinationIdnum2;

    /** 资料 */
    private String fileInfo;
}