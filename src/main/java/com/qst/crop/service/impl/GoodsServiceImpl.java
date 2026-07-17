package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.entity.Goods;
import com.qst.crop.service.FollowService;
import com.qst.crop.service.GoodsService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowService followService;

    @Override
    public List<Goods> listByType(String type, String keyword) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        // 只有当 type 不为空时才添加过滤条件
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq("type", type);
        }
        wrapper.orderByDesc("create_time");
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
        // 设置默认状态：1-上架/有效
        if (goods.getGoodsStatus() == null) {
            goods.setGoodsStatus(1);
        }
        goodsMapper.insert(goods);
        notifyGoodsPublished(goods);
        return goods;
    }

    /**
     * 商品/需求发布后按类型广播通知：
     * - 任意发布：通知管理员「商品增加」（平台监管）
     * - type=1（供应商品）：只通知「关注该农户」的买家，避免打扰全部买家
     * - type=2（求购需求）：不再通知农户（买家过多会刷屏），农户可在求购大厅主动浏览
     */
    private void notifyGoodsPublished(Goods goods) {
        Integer id = goods.getGoodsId();
        String title = goods.getTitle() != null ? goods.getTitle() : "未命名";
        String type = goods.getType();
        try {
            notificationService.sendToRole("admin", "商品增加",
                    "有新的商品/需求《" + title + "》发布，请关注。", "goods_new", id);
            if ("1".equals(type)) {
                // 仅推送给该农户的粉丝买家
                String owner = goods.getOwnName();
                List<String> followers = followService.listFollowerNames(owner);
                String content = "您关注的农户发布了新商品《" + title + "》，快去看看吧。";
                for (String fan : followers) {
                    notificationService.send(fan, "新增商品通知", content, "goods_new", id);
                }
                log.info("商品通知按关注过滤: goodsId={}, owner={}, 推送={} 个粉丝", id, owner, followers.size());
            }
            // type=2（求购需求）：不对农户广播通知
        } catch (Exception e) {
            log.warn("商品发布通知发送失败: goodsId={}, err={}", id, e.getMessage());
        }
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
