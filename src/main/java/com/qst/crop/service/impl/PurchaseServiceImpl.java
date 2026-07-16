package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qst.crop.dao.AddressMapper;
import com.qst.crop.dao.GoodsMapper;
import com.qst.crop.dao.PurchaseDetailMapper;
import com.qst.crop.dao.PurchaseMapper;
import com.qst.crop.dao.SellPurchaseMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.Address;
import com.qst.crop.entity.Goods;
import com.qst.crop.entity.Purchase;
import com.qst.crop.entity.PurchaseDetail;
import com.qst.crop.entity.SellPurchase;
import com.qst.crop.entity.User;
import com.qst.crop.model.CreateOrderDTO;
import com.qst.crop.model.OrderItemVO;
import com.qst.crop.model.OrderVO;
import com.qst.crop.model.ShipDTO;
import com.qst.crop.service.NotificationService;
import com.qst.crop.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private PurchaseMapper purchaseMapper;
    @Autowired
    private PurchaseDetailMapper purchaseDetailMapper;
    @Autowired
    private SellPurchaseMapper sellPurchaseMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Integer createOrder(String buyerName, CreateOrderDTO dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return null;
        }
        // 收货地址快照（必须是当前买家自己的地址）
        Address addr = addressMapper.selectById(dto.getAddressId());
        if (addr == null || !buyerName.equals(addr.getOwnName())) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // 主订单（买家侧）
        Purchase purchase = new Purchase();
        purchase.setOwnName(buyerName);
        purchase.setPurchaseType(1); // 1=买到的
        purchase.setTotalPrice(dto.getTotalPrice());
        purchase.setPurchaseStatus(2); // 2=待发货（本流程无付款环节）
        purchase.setAddress(addr.getAddressDetail());
        purchase.setConsignee(addr.getConsignee());
        purchase.setConsigneePhone(addr.getPhone());
        purchase.setCreateTime(now);
        purchase.setUpdateTime(now);
        purchaseMapper.insert(purchase);
        Integer purchaseId = purchase.getPurchaseId();

        // 明细 + 卖家侧订单 + 通知卖家
        Set<String> notifiedSellers = new HashSet<>();
        for (CreateOrderDTO.OrderItem item : dto.getItems()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            String sellerName = goods != null ? goods.getOwnName() : null;

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchaseId(purchaseId);
            detail.setGoodsId(item.getGoodsId());
            detail.setUninPrice(item.getUninPrice());
            detail.setSumPrice(item.getUninPrice().multiply(BigDecimal.valueOf(item.getCount())));
            detail.setCount(item.getCount());
            purchaseDetailMapper.insert(detail);

            SellPurchase sell = new SellPurchase();
            sell.setPurchaseId(purchaseId);
            sell.setOwnName(sellerName);
            sell.setPurchaseType(2); // 2=卖出的
            sell.setUninPrice(item.getUninPrice());
            sell.setSumPrice(detail.getSumPrice());
            sell.setPurchaseStatus(2); // 待发货
            sell.setGoodsId(item.getGoodsId());
            sell.setAddress(""); // 发货地址在发货时填写，先置空以满足非空约束
            sell.setCreateTime(now);
            sell.setUpdateTime(now);
            sellPurchaseMapper.insert(sell);

            // 给每个卖家发一条购买通知（去重）
            if (sellerName != null && !sellerName.equals(buyerName) && notifiedSellers.add(sellerName)) {
                String content = "买家 " + buyerName + " 购买了您的商品"
                        + (goods != null ? "《" + goods.getTitle() + "》" : "")
                        + "，请及时发货";
                notificationService.send(sellerName, "您有新的订单", content, "order", purchaseId);
            }
        }
        return purchaseId;
    }

    @Override
    public List<OrderVO> listBought(String buyerName) {
        List<Purchase> purchases = purchaseMapper.selectList(
                new QueryWrapper<Purchase>().eq("own_name", buyerName).orderByDesc("create_time"));
        List<OrderVO> result = new ArrayList<>();
        for (Purchase p : purchases) {
            OrderVO vo = new OrderVO();
            vo.setPurchaseId(p.getPurchaseId());
            vo.setStatus(p.getPurchaseStatus());
            vo.setStatusText(statusText(p.getPurchaseStatus()));
            vo.setTotalPrice(p.getTotalPrice());
            vo.setCreateTime(fmt(p.getCreateTime()));
            vo.setConsignee(p.getConsignee());
            vo.setConsigneePhone(p.getConsigneePhone());
            vo.setAddress(p.getAddress());
            vo.setItems(buildItems(p.getPurchaseId(), null));
            // 发货信息（从卖家侧订单取，取一条）
            SellPurchase sp = sellPurchaseMapper.selectOne(
                    new QueryWrapper<SellPurchase>().eq("purchase_id", p.getPurchaseId()).last("LIMIT 1"));
            if (sp != null) {
                vo.setShipName(sp.getShipName());
                vo.setShipPhone(sp.getShipPhone());
                vo.setShipAddress(sp.getAddress());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<OrderVO> listSold(String sellerName) {
        List<SellPurchase> sells = sellPurchaseMapper.selectList(
                new QueryWrapper<SellPurchase>().eq("own_name", sellerName).orderByDesc("create_time"));
        List<OrderVO> result = new ArrayList<>();
        for (SellPurchase sp : sells) {
            Purchase p = purchaseMapper.selectById(sp.getPurchaseId());
            OrderVO vo = new OrderVO();
            vo.setPurchaseId(sp.getPurchaseId());
            vo.setStatus(sp.getPurchaseStatus());
            vo.setStatusText(statusText(sp.getPurchaseStatus()));
            vo.setTotalPrice(sp.getSumPrice());
            vo.setCreateTime(fmt(p != null ? p.getCreateTime() : sp.getCreateTime()));
            // 买家收货信息（来自买家侧订单）
            if (p != null) {
                vo.setConsignee(p.getConsignee());
                vo.setConsigneePhone(p.getConsigneePhone());
                vo.setAddress(p.getAddress());
            }
            // 发货信息（本卖家填写）
            vo.setShipName(sp.getShipName());
            vo.setShipPhone(sp.getShipPhone());
            vo.setShipAddress(sp.getAddress());
            vo.setItems(buildItems(sp.getPurchaseId(), sp.getGoodsId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean ship(Integer purchaseId, String sellerName, ShipDTO dto) {
        List<SellPurchase> sells = sellPurchaseMapper.selectList(
                new QueryWrapper<SellPurchase>().eq("purchase_id", purchaseId).eq("own_name", sellerName));
        if (sells.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        for (SellPurchase sp : sells) {
            sp.setAddress(dto.getShipAddress());
            sp.setShipName(dto.getShipName());
            sp.setShipPhone(dto.getShipPhone());
            sp.setPurchaseStatus(3); // 待收货
            sp.setUpdateTime(now);
            sellPurchaseMapper.updateById(sp);
        }
        // 同步买家侧订单状态并通知买家
        Purchase p = purchaseMapper.selectById(purchaseId);
        if (p != null) {
            Integer old = p.getPurchaseStatus();
            if (old == null || old < 3) {
                p.setPurchaseStatus(3);
                p.setUpdateTime(now);
                purchaseMapper.updateById(p);
                notificationService.send(p.getOwnName(), "您的订单已发货",
                        "订单 #" + purchaseId + " 已发货，请注意查收", "order", purchaseId);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean receive(Integer purchaseId, String buyerName) {
        Purchase p = purchaseMapper.selectById(purchaseId);
        if (p == null || !buyerName.equals(p.getOwnName())) {
            return false;
        }
        // 幂等：仅当订单尚未"已收货"时才发放信誉分并通知，避免重复点击/重放导致农户信誉分被多次累加
        boolean firstReceive = p.getPurchaseStatus() == null || p.getPurchaseStatus() != 4;
        LocalDateTime now = LocalDateTime.now();
        p.setPurchaseStatus(4); // 已收货
        p.setUpdateTime(now);
        purchaseMapper.updateById(p);

        // 同步卖家侧订单状态
        List<SellPurchase> sells = sellPurchaseMapper.selectList(
                new QueryWrapper<SellPurchase>().eq("purchase_id", purchaseId));
        Set<String> sellers = new HashSet<>();
        for (SellPurchase sp : sells) {
            sp.setPurchaseStatus(4);
            sp.setUpdateTime(now);
            sellPurchaseMapper.updateById(sp);
            if (sp.getOwnName() != null) {
                sellers.add(sp.getOwnName());
            }
        }
        // 首次确认收货：交易完成，给每个农户（卖家）信誉分 +2 并通知
        if (firstReceive) {
            for (String seller : sellers) {
                rewardCredit(seller, 2);
                notificationService.send(seller, "买家已确认收货",
                        "订单 #" + purchaseId + " 买家已确认收货，您的信誉分 +2", "order", purchaseId);
            }
        }
        return true;
    }

    /** 增加用户信誉分：使用 SQL 原子自增，避免并发丢失更新；credit 为 NULL 时按 0 计算 */
    private void rewardCredit(String userName, int delta) {
        if (userName == null || delta == 0) {
            return;
        }
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("user_name", userName)
                .setSql("credit = IFNULL(credit, 0) + " + delta));
    }

    /** 组装商品明细；goodsId 非空时只取该商品（卖家视角），为空时取整单全部（买家视角） */
    private List<OrderItemVO> buildItems(Integer purchaseId, Integer goodsId) {
        QueryWrapper<PurchaseDetail> w = new QueryWrapper<PurchaseDetail>().eq("purchase_id", purchaseId);
        if (goodsId != null) {
            w.eq("goods_id", goodsId);
        }
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(w);
        List<OrderItemVO> items = new ArrayList<>();
        for (PurchaseDetail d : details) {
            OrderItemVO it = new OrderItemVO();
            it.setGoodsId(d.getGoodsId());
            Goods g = goodsMapper.selectById(d.getGoodsId());
            it.setTitle(g != null ? g.getTitle() : "商品");
            it.setUninPrice(d.getUninPrice());
            it.setCount(d.getCount());
            it.setSumPrice(d.getSumPrice());
            items.add(it);
        }
        return items;
    }

    private String statusText(Integer s) {
        if (s == null) return "未知";
        switch (s) {
            case 1: return "待付款";
            case 2: return "待发货";
            case 3: return "待收货";
            case 4: return "已收货";
            default: return "未知";
        }
    }

    private String fmt(LocalDateTime t) {
        return t == null ? null : t.format(FMT);
    }
}
