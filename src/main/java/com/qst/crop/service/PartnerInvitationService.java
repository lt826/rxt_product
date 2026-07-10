package com.qst.crop.service;

import com.qst.crop.entity.PartnerInvitation;
import com.qst.crop.model.InvitationVO;
import com.qst.crop.model.PartnerInvitationDTO;

import java.util.List;

/**
 * 联合贷款人邀请服务接口
 */
public interface PartnerInvitationService {

    /**
     * 发送邀请
     */
    PartnerInvitation sendInvitation(String applicantUserName, PartnerInvitationDTO dto);

    /**
     * 获取我收到的邀请（作为被邀请人）
     */
    List<InvitationVO> getReceivedInvitations(String userName);

    /**
     * 获取我发送的邀请（作为申请人）
     */
    List<InvitationVO> getSentInvitations(String userName);

    /**
     * 回复邀请（同意或拒绝）
     */
    boolean replyInvitation(Integer invitationId, Integer status, String remark);

    /**
     * 获取邀请详情
     */
    PartnerInvitation getById(Integer invitationId);

    /**
     * 清除我收到的全部邀请（作为被邀请人）
     * @param userName 当前用户名
     * @return 删除条数
     */
    int clearReceivedInvitations(String userName);

    /**
     * 清除我发送的全部邀请（作为申请人）
     * @param userName 当前用户名
     * @return 删除条数
     */
    int clearSentInvitations(String userName);

    /**
     * 验证用户是否有已同意的联合贷款人
     * @param userName 用户名
     * @return 已同意的联合贷款人数量
     */
    long countAgreedPartners(String userName);
}
