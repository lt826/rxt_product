package com.qst.crop.model;

import lombok.Data;

/**
 * 联合贷款人邀请VO
 */
@Data
public class InvitationVO {
    private Integer id;
    private String applicantRealName;
    private String applicantPhone;
    private String inviteeRealName;
    private String inviteePhone;
    private String projectDescription;
    private Integer status;
    private String createTime;
    private String replyTime;
    private String replyRemark;
}
