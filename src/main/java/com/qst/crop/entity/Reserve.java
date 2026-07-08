package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专家预约表实体类
 */
@Data
@TableName("tb_reserve")
public class Reserve {

    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 专家用户名 */
    private String expertName;

    /** 咨询者用户名 */
    private String questioner;

    /** 面积 */
    private String area;

    /** 土地地址 */
    private String address;

    /** 农作物名称 */
    private String plantName;

    /** 土壤条件 */
    private String soilCondition;

    /** 作物条件 */
    private String plantCondition;

    /** 作物详细信息 */
    private String plantDetail;

    /** 手机号 */
    private String phone;

    /** 买家/农户留言 */
    private String message;

    /** 专家回复意向 */
    private String answer;

    /** 状态，0：申请中，1：完成 */
    private Integer status;
}