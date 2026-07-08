package com.qst.crop.service;

import com.qst.crop.entity.Reserve;

import java.util.List;

public interface ReserveService {

    /** 农户申请专家出诊预约 */
    Reserve apply(Reserve reserve);

    /** 农户查看自己的预约 */
    List<Reserve> listByQuestioner(String questioner);

    /** 专家查看提问给自己的预约 */
    List<Reserve> listForExpert(String expertName);

    /** 专家处理预约（回复意向 + 状态） */
    Reserve handle(Integer id, String expertName, String answer, Integer status);
}
