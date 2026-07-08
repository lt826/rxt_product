package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 地址表实体类
 */
@Data
@TableName("tb_address")
public class Address {

    /** 地址id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户名 */
    private String ownName;

    /** 收货人 */
    private String consignee;

    /** 手机号 */
    private String phone;

    /** 收货地址 */
    private String addressDetail;

    /** 默认地址，0不默认，1默认 */
    private Integer isDefault;
}