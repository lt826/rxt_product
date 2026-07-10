package com.qst.crop.service;

import com.qst.crop.entity.FinancingIntention;
import com.qst.crop.model.FinancingIntentionDTO;

import java.util.List;

/**
 * 融资意向服务接口
 */
public interface FinancingIntentionService {

    /**
     * 提交融资意向
     */
    FinancingIntention submit(String userName, FinancingIntentionDTO dto);

    /**
     * 获取我的融资意向列表
     */
    List<FinancingIntention> getMyIntentions(String userName);

    /**
     * 获取融资意向详情
     */
    FinancingIntention getById(Integer id);

    /**
     * 更新融资意向
     */
    boolean update(Integer id, FinancingIntentionDTO dto);

    /**
     * 删除融资意向
     */
    boolean delete(Integer id);
}
