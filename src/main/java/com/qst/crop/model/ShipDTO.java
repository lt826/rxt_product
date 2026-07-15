package com.qst.crop.model;

import lombok.Data;

/**
 * 农户发货入参
 */
@Data
public class ShipDTO {
    /** 发货地址 */
    private String shipAddress;
    /** 发货人姓名 */
    private String shipName;
    /** 发货人电话 */
    private String shipPhone;
}
