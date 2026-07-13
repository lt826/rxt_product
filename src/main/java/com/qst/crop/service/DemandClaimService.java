package com.qst.crop.service;

import com.qst.crop.entity.DemandClaim;

import java.util.List;

/**
 * 求购需求接取服务
 */
public interface DemandClaimService {

    /** 农户接取一条求购需求（校验通过后写记录，并自动给发布者发首条消息开启私聊） */
    DemandClaim claim(String claimerName, Integer demandId);

    /** 查询某条求购需求的所有接取者（发布者查看用） */
    List<DemandClaim> listByDemand(Integer demandId);

    /** 查询当前用户已接取的需求记录（农户在求购大厅标"已接取"用） */
    List<DemandClaim> listMyClaims(String claimerName);
}
