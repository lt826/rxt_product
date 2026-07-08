package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专家信息表实体类
 */
@Data
@TableName("tb_expert")
public class Expert {

    /** 用户名（主键） */
    @TableId("user_name")
    private String userName;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 从事专业 */
    private String profession;

    /** 职位 */
    private String position;

    /** 所属单位 */
    private String belong;
}