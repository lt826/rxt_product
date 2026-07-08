package com.qst.crop.service;

import com.qst.crop.model.CreateOrderDTO;

public interface PurchaseService {
    Integer createOrder(String buyerName, CreateOrderDTO dto);
}
