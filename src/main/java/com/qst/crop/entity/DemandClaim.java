package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求购需求接取记录实体类
 */
@Data
@TableName("tb_demand_claim")
public class DemandClaim {

    /** 接取记录id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 求购需求id(关联 tb_goods.goods_id, type=2) */
    private Integer demandId;

    /** 接取农户用户名 */
    private String claimerName;

    /** 需求发布者用户名 */
    private String demandOwnerName;

    /** 状态: 0待响应 1已同意 2已拒绝 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
