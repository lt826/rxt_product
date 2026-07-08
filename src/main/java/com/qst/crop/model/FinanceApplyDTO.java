package com.qst.crop.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinanceApplyDTO {
    private Integer bankId;
    private BigDecimal money;
    private BigDecimal rate;
    private Integer repayment;
    private String combinationName1;
    private String combinationPhone1;
    private String combinationIdnum1;
    private String combinationName2;
    private String combinationPhone2;
    private String combinationIdnum2;
    private String fileInfo;
}
