package com.qst.crop.service;

import com.qst.crop.entity.Discuss;

import java.util.List;

public interface DiscussService {

    /** 发布知识评论 */
    Discuss publish(Discuss discuss);

    /** 查询某条知识下的评论 */
    List<Discuss> listByKnowledge(Integer knowledgeId);
}
