package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.DiscussMapper;
import com.qst.crop.entity.Discuss;
import com.qst.crop.service.DiscussService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiscussServiceImpl implements DiscussService {

    @Autowired
    private DiscussMapper discussMapper;

    @Override
    public Discuss publish(Discuss discuss) {
        discuss.setCreateTime(LocalDateTime.now());
        discussMapper.insert(discuss);
        return discuss;
    }

    @Override
    public List<Discuss> listByKnowledge(Integer knowledgeId) {
        QueryWrapper<Discuss> wrapper = new QueryWrapper<>();
        wrapper.eq("knowledge_id", knowledgeId).orderByAsc("create_time");
        return discussMapper.selectList(wrapper);
    }
}
