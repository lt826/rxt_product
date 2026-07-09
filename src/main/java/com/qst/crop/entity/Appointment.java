package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专家预约表实体类
 */
@Data
@TableName("tb_appointment")
public class Appointment {

    /** 预约ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 农户用户名 */
    private String farmerName;

    /** 专家用户名 */
    private String expertName;

    /** 专家真实姓名（冗余字段） */
    private String expertRealName;

    /** 预约地址 */
    private String address;

    /** 预约时间 */
    private LocalDateTime appointmentTime;

    /** 预约说明 */
    private String description;

    /** 专家回复/原因 */
    private String reply;

    /** 状态：pending-待审批，approved-已同意，rejected-已拒绝，completed-已完成，cancelled-已取消 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
