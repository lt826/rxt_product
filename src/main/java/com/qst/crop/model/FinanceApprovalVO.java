package com.qst.crop.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行端融资审批列表 VO
 */
@Data
public class FinanceApprovalVO {

    /** 申请编号 */
    private Integer financeId;

    /** 农户真实姓名 */
    private String realName;

    /** 农户用户名 */
    private String ownName;

    /** 申请金额 */
    private BigDecimal money;

    /** 年利率 */
    private BigDecimal rate;

    /** 还款期限（月） */
    private Integer repayment;

    /** 联合贷款人姓名（"张三、李四" 或 "—"） */
    private String coLenderNames;

    /** 联合贷款人人数 */
    private Integer coLenderCount;

    /** 农户信誉分 */
    private Integer credit;

    /** 匹配度（0~100） */
    private Double matchScore;

    /** 状态：0申请中/1通过/2驳回 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
