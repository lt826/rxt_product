package com.qst.crop.model;

import lombok.Data;

@Data
public class RecommendPartnerVO {
    private String userName;
    private String realName;
    private String phone;
    private Integer credit;
    private Double matchScore;
}
