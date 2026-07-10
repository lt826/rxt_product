package com.qst.crop.model;

import lombok.Data;

/**
 * 融资意向提交DTO
 */
@Data
public class FinancingIntentionDTO {
    /** 融资金额 */
    private Integer amount;

    /** 融资用途 */
    private String application;

    /** 融资农作物 */
    private String item;

    /** 还款日期（月份） */
    private Integer repaymentPeriod;

    /** 种植面积（亩） */
    private Integer area;
}
