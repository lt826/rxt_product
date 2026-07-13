package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配规则权重配置实体类
 */
@Data
@TableName("tb_match_rule")
public class MatchRule {

    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 联合贷款人权重(%) */
    private BigDecimal partnerWeight;

    /** 信誉分权重(%) */
    private BigDecimal creditWeight;

    /** 申请金额权重(%) */
    private BigDecimal amountWeight;

    /** 期限权重(%) */
    private BigDecimal termWeight;

    /** 金额归一化上限 */
    private BigDecimal amountUpperBound;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
