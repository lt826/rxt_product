package com.qst.crop.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {
    private Integer addressId;
    private BigDecimal totalPrice;
    private List<OrderItem> items;

    @Data
    public static class OrderItem {
        private Integer goodsId;
        private BigDecimal uninPrice;
        private Integer count;
    }
}
