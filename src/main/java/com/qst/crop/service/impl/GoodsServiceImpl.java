package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.entity.Goods;
import com.qst.crop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Goods> listByType(String type, String keyword) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type).orderByDesc("create_time");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Goods publish(Goods goods) {
        LocalDateTime now = LocalDateTime.now();
        goods.setCreateTime(now);
        goods.setUpdateTime(now);
        goodsMapper.insert(goods);
        return goods;
    }

    @Override
    public Goods update(Goods goods) {
        Goods exist = goodsMapper.selectById(goods.getGoodsId());
        if (exist == null) throw new RuntimeException("商品/需求不存在");
        if (goods.getOwnName() != null && !goods.getOwnName().equals(exist.getOwnName())) {
            throw new RuntimeException("无权修改他人信息");
        }
        if (goods.getTitle() != null) exist.setTitle(goods.getTitle());
        if (goods.getPrice() != null) exist.setPrice(goods.getPrice());
        if (goods.getContent() != null) exist.setContent(goods.getContent());
        if (goods.getPicture() != null) exist.setPicture(goods.getPicture());
        if (goods.getAddress() != null) exist.setAddress(goods.getAddress());
        exist.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(exist);
        return exist;
    }

    @Override
    public boolean delete(Integer id, String ownName) {
        Goods exist = goodsMapper.selectById(id);
        if (exist == null) return false;
        if (ownName != null && !ownName.equals(exist.getOwnName())) {
            throw new RuntimeException("无权删除他人信息");
        }
        return goodsMapper.deleteById(id) > 0;
    }

    @Override
    public List<Goods> listByOwner(String ownName, String type) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("own_name", ownName);
        if (type != null && !type.isEmpty()) wrapper.eq("type", type);
        wrapper.orderByDesc("create_time");
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Goods detail(Integer id) {
        return goodsMapper.selectById(id);
    }
}
