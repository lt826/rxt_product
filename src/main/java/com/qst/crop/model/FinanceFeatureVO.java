package com.qst.crop.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页「金融产品」精选展示对象
 * <p>
 * 融资产品实体只有 bankId，这里在解析时补上银行名，供首页与后台直接展示。
 */
@Data
public class FinanceFeatureVO {

    private Integer id;

    /** 银行名 */
    private String bankName;

    /** 产品名 */
    private String name;

    /** 最高额度 */
    private BigDecimal maxQuota;

    /** 最低额度 */
    private BigDecimal minQuota;

    /** 年利率（%） */
    private BigDecimal rate;

    /** 最长期限（月） */
    private Integer maxTerm;

    /** 最短期限（月） */
    private Integer minTerm;

    /** 产品介绍 */
    private String description;
}
