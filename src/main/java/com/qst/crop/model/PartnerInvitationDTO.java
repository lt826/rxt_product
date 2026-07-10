package com.qst.crop.model;

import lombok.Data;

/**
 * 联合贷款人邀请DTO
 */
@Data
public class PartnerInvitationDTO {
    /** 被邀请人姓名 */
    private String inviteeName;

    /** 被邀请人电话 */
    private String inviteePhone;

    /** 融资项目描述 */
    private String projectDescription;
}
