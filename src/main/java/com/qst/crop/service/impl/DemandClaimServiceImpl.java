package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.DemandClaimMapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.entity.DemandClaim;
import com.qst.crop.entity.Goods;
import com.qst.crop.service.DemandClaimService;
import com.qst.crop.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求购需求接取服务实现
 */
@Slf4j
@Service
public class DemandClaimServiceImpl implements DemandClaimService {

    @Autowired
    private DemandClaimMapper demandClaimMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public DemandClaim claim(String claimerName, Integer demandId) {
        // 1. 校验需求存在且为求购类型(type=2)
        Goods goods = goodsMapper.selectById(demandId);
        if (goods == null) {
            throw new RuntimeException("求购需求不存在");
        }
        if (!"2".equals(goods.getType())) {
            throw new RuntimeException("该记录不是求购需求，无法接取");
        }
        String owner = goods.getOwnName();
        if (owner == null || owner.equals(claimerName)) {
            throw new RuntimeException("不能接取自己发布的需求");
        }

        // 2. 校验是否已接取过（同一农户对同一需求只能接一次）
        QueryWrapper<DemandClaim> qw = new QueryWrapper<>();
        qw.eq("demand_id", demandId).eq("claimer_name", claimerName);
        List<DemandClaim> existing = demandClaimMapper.selectList(qw);
        if (existing != null && !existing.isEmpty()) {
            throw new RuntimeException("您已接取过该需求");
        }

        // 3. 写接取记录
        DemandClaim claim = new DemandClaim();
        claim.setDemandId(demandId);
        claim.setClaimerName(claimerName);
        claim.setDemandOwnerName(owner);
        claim.setStatus(0);
        LocalDateTime now = LocalDateTime.now();
        claim.setCreateTime(now);
        claim.setUpdateTime(now);
        demandClaimMapper.insert(claim);

        // 4. 自动给发布者发首条消息，开启私聊
        String title = goods.getTitle() != null ? goods.getTitle() : "";
        String content = "我对您的求购【" + title + "】感兴趣，已接单，期待与您详谈。";
        try {
            messageService.sendMessage(claimerName, owner, content);
        } catch (Exception e) {
            // 消息发送失败不回滚接取记录（接取已成功，私聊用户仍可主动发起）
            log.warn("接取后发送首条消息失败: claimer={}, owner={}, err={}",
                    claimerName, owner, e.getMessage());
        }

        log.info("农户接取求购需求: demandId={}, claimer={}, owner={}", demandId, claimerName, owner);
        return claim;
    }

    @Override
    public List<DemandClaim> listByDemand(Integer demandId) {
        QueryWrapper<DemandClaim> qw = new QueryWrapper<>();
        qw.eq("demand_id", demandId).orderByDesc("create_time");
        return demandClaimMapper.selectList(qw);
    }

    @Override
    public List<DemandClaim> listMyClaims(String claimerName) {
        QueryWrapper<DemandClaim> qw = new QueryWrapper<>();
        qw.eq("claimer_name", claimerName).orderByDesc("create_time");
        return demandClaimMapper.selectList(qw);
    }
}
