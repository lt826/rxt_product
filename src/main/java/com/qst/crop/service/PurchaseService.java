package com.qst.crop.service;

import com.qst.crop.model.CreateOrderDTO;
import com.qst.crop.model.OrderVO;
import com.qst.crop.model.ShipDTO;

import java.util.List;

public interface PurchaseService {
    /** 买家下单 */
    Integer createOrder(String buyerName, CreateOrderDTO dto);

    /** 买家"我买到的"订单 */
    List<OrderVO> listBought(String buyerName);

    /** 卖家"我卖出的"订单 */
    List<OrderVO> listSold(String sellerName);

    /** 农户发货：填写物流信息并把状态改为待收货 */
    boolean ship(Integer purchaseId, String sellerName, ShipDTO dto);

    /** 买家确认收货：状态改为已收货 */
    boolean receive(Integer purchaseId, String buyerName);
}
