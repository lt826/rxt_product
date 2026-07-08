package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.KnowledgeMapper;
import com.qst.crop.entity.Knowledge;
import com.qst.crop.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Override
    public List<Knowledge> list(String keyword) {
        QueryWrapper<Knowledge> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        return knowledgeMapper.selectList(wrapper);
    }

    @Override
    public Knowledge detail(Integer id) {
        return knowledgeMapper.selectById(id);
    }

    @Override
    public Knowledge publish(Knowledge knowledge) {
        LocalDateTime now = LocalDateTime.now();
        knowledge.setCreateTime(now);
        knowledge.setUpdateTime(now);
        knowledgeMapper.insert(knowledge);
        return knowledge;
    }

    @Override
    public Knowledge update(Knowledge knowledge) {
        Knowledge exist = knowledgeMapper.selectById(knowledge.getKnowledgeId());
        if (exist == null) {
            throw new RuntimeException("知识不存在");
        }
        if (knowledge.getOwnName() != null && !knowledge.getOwnName().equals(exist.getOwnName())) {
            throw new RuntimeException("无权修改他人的知识");
        }
        exist.setTitle(knowledge.getTitle());
        exist.setContent(knowledge.getContent());
        if (knowledge.getPicPath() != null) {
            exist.setPicPath(knowledge.getPicPath());
        }
        exist.setUpdateTime(LocalDateTime.now());
        knowledgeMapper.updateById(exist);
        return exist;
    }

    @Override
    public boolean delete(Integer id, String ownName) {
        Knowledge exist = knowledgeMapper.selectById(id);
        if (exist == null) {
            return false;
        }
        if (ownName != null && !ownName.equals(exist.getOwnName())) {
            throw new RuntimeException("无权删除他人的知识");
        }
        return knowledgeMapper.deleteById(id) > 0;
    }

    @Override
    public List<Knowledge> listByOwner(String ownName) {
        QueryWrapper<Knowledge> wrapper = new QueryWrapper<>();
        wrapper.eq("own_name", ownName).orderByDesc("update_time");
        return knowledgeMapper.selectList(wrapper);
    }
}
