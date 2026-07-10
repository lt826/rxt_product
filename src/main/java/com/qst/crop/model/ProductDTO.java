package com.qst.crop.model;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 融资产品DTO
 */
@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private BigDecimal minQuota;
    private BigDecimal maxQuota;
    private BigDecimal rate;
    private Integer minTerm;
    private Integer maxTerm;
    private String description;
    private String contactPhone;
    private Integer status;
}
