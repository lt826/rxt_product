package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 融资产品实体类
 */
@Data
@TableName("tb_financing_product")
public class FinancingProduct {

    /** 产品ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 银行ID */
    private Integer bankId;

    /** 产品名称 */
    private String name;

    /** 最低额度 */
    private BigDecimal minQuota;

    /** 最高额度 */
    private BigDecimal maxQuota;

    /** 年利率（%） */
    private BigDecimal rate;

    /** 最短期限（月） */
    private Integer minTerm;

    /** 最长期限（月） */
    private Integer maxTerm;

    /** 产品介绍 */
    private String description;

    /** 联系电话 */
    private String contactPhone;

    /** 状态：0-停用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
