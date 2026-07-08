package com.qst.crop.service.impl;

import com.qst.crop.dao.PurchaseMapper;
import com.qst.crop.dao.PurchaseDetailMapper;
import com.qst.crop.dao.SellPurchaseMapper;
import com.qst.crop.entity.Purchase;
import com.qst.crop.entity.PurchaseDetail;
import com.qst.crop.entity.SellPurchase;
import com.qst.crop.model.CreateOrderDTO;
import com.qst.crop.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private PurchaseDetailMapper purchaseDetailMapper;

    @Autowired
    private SellPurchaseMapper sellPurchaseMapper;

    @Override
    @Transactional
    public Integer createOrder(String buyerName, CreateOrderDTO dto) {
        // 创建主订单
        Purchase purchase = new Purchase();
        purchase.setOwnName(buyerName);
        purchase.setPurchaseType(1); // 1=买到的
        purchase.setTotalPrice(dto.getTotalPrice());
        purchase.setPurchaseStatus(1); // 1=待付款
        purchase.setCreateTime(LocalDateTime.now());
        purchase.setUpdateTime(LocalDateTime.now());
        purchaseMapper.insert(purchase);

        // 创建订单详情和卖家订单
        for (CreateOrderDTO.OrderItem item : dto.getItems()) {
            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchaseId(purchase.getPurchaseId());
            detail.setGoodsId(item.getGoodsId());
            detail.setUninPrice(item.getUninPrice());
            detail.setSumPrice(item.getUninPrice().multiply(java.math.BigDecimal.valueOf(item.getCount())));
            detail.setCount(item.getCount());
            purchaseDetailMapper.insert(detail);

            // 简化：卖家订单关联（实际应通过 goods 查 ownName）
            SellPurchase sell = new SellPurchase();
            sell.setPurchaseId(purchase.getPurchaseId());
            sell.setUninPrice(item.getUninPrice());
            sell.setSumPrice(detail.getSumPrice());
            sell.setPurchaseStatus(1);
            sell.setGoodsId(item.getGoodsId());
            sell.setCreateTime(LocalDateTime.now());
            sell.setUpdateTime(LocalDateTime.now());
            sellPurchaseMapper.insert(sell);
        }
        return purchase.getPurchaseId();
    }
}
