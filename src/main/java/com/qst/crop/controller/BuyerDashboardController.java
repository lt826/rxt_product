package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Bank;
import com.qst.crop.entity.Goods;
import com.qst.crop.entity.User;
import com.qst.crop.dao.BankMapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.dao.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 买家工作台数据聚合接口
 */
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerDashboardController {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BankMapper bankMapper;

    /**
     * 获取买家工作台统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(Authentication auth) {
        String username = auth.getName();
        Map<String, Object> stats = new HashMap<>();

        // 在售商品数（买家发布的 type='2' 的 goods）
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("own_name", username);
        goodsWrapper.eq("type", "2");
        long demandCount = goodsMapper.selectCount(goodsWrapper);
        stats.put("demandCount", demandCount);

        // 待联系农户数（所有 farmer 角色的用户）
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "farmer");
        long farmerCount = userMapper.selectCount(userWrapper);
        stats.put("farmerCount", farmerCount);

        // 今日询价：当天发布的 type='1' 的商品数（卖家供应）
        QueryWrapper<Goods> sellWrapper = new QueryWrapper<>();
        sellWrapper.eq("type", "1");
        long productCount = goodsMapper.selectCount(sellWrapper);
        stats.put("productCount", productCount);

        // 可申请的融资产品数（银行提供的产品数）
        QueryWrapper<Bank> bankWrapper = new QueryWrapper<>();
        long productCountBank = bankMapper.selectCount(bankWrapper);
        stats.put("financingProductCount", productCountBank);

        return Result.success("查询成功", stats);
    }

    /**
     * 获取买家发布的收购需求列表（用于工作台展示）
     */
    @GetMapping("/demands")
    public Result<List<Goods>> getDemands(Authentication auth) {
        String username = auth.getName();
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("own_name", username);
        wrapper.eq("type", "2");
        wrapper.orderByDesc("create_time");
        List<Goods> list = goodsMapper.selectList(wrapper);
        return Result.success("查询成功", list);
    }

    /**
     * 获取买家工作台匹配数据（基于买家需求和卖家供应的匹配）
     */
    @GetMapping("/matches")
    public Result<List<Map<String, Object>>> getMatches(Authentication auth) {
        String username = auth.getName();

        // 获取买家发布的收购需求
        QueryWrapper<Goods> demandWrapper = new QueryWrapper<>();
        demandWrapper.eq("own_name", username);
        demandWrapper.eq("type", "2");
        demandWrapper.orderByDesc("create_time");
        demandWrapper.last("LIMIT 10");
        List<Goods> demands = goodsMapper.selectList(demandWrapper);

        // 获取所有在售商品（供应）
        QueryWrapper<Goods> supplyWrapper = new QueryWrapper<>();
        supplyWrapper.eq("type", "1");
        supplyWrapper.eq("goods_status", 1);
        supplyWrapper.orderByDesc("create_time");
        supplyWrapper.last("LIMIT 10");
        List<Goods> supplies = goodsMapper.selectList(supplyWrapper);

        List<Map<String, Object>> matches = new java.util.ArrayList<>();
        // 简单匹配：按标题关键词模糊匹配需求和供应
        int i = 0;
        for (Goods demand : demands) {
            if (i >= 5) break;
            Map<String, Object> match = new HashMap<>();
            match.put("product", demand.getTitle());
            match.put("amount", demand.getContent() != null && demand.getContent().length() > 50
                    ? demand.getContent().substring(0, 50) + "..."
                    : demand.getContent());
            match.put("farmer", "待分配");
            match.put("status", "待确认");

            // 尝试匹配一个供应
            for (Goods supply : supplies) {
                if (supply.getTitle().contains(demand.getTitle().substring(0, Math.min(3, demand.getTitle().length())))
                        || demand.getTitle().contains(supply.getTitle().substring(0, Math.min(3, supply.getTitle().length())))) {
                    match.put("farmer", supply.getOwnName());
                    match.put("status", "已匹配");
                    break;
                }
            }

            matches.add(match);
            i++;
        }
        return Result.success("查询成功", matches);
    }
}
