package com.qst.crop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联合贷款人邀请实体类
 */
@Data
@TableName("tb_partner_invitation")
public class PartnerInvitation {

    /** 邀请ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 申请人用户名 */
    private String applicantUserName;

    /** 申请人真实姓名 */
    private String applicantRealName;

    /** 申请人电话 */
    private String applicantPhone;

    /** 被邀请人用户名 */
    private String inviteeUserName;

    /** 被邀请人真实姓名 */
    private String inviteeRealName;

    /** 被邀请人电话 */
    private String inviteePhone;

    /** 融资项目描述 */
    private String projectDescription;

    /** 状态：0-待处理，1-已同意，2-已拒绝 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 回复备注 */
    private String replyRemark;
}
