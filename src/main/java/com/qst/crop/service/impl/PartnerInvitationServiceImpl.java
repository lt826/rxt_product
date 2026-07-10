package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.PartnerInvitationMapper;
import com.qst.crop.dao.UserMapper;
import com.qst.crop.entity.PartnerInvitation;
import com.qst.crop.entity.User;
import com.qst.crop.model.InvitationVO;
import com.qst.crop.model.PartnerInvitationDTO;
import com.qst.crop.service.MessageService;
import com.qst.crop.service.NotificationService;
import com.qst.crop.service.PartnerInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 联合贷款人邀请服务实现类
 */
@Slf4j
@Service
public class PartnerInvitationServiceImpl implements PartnerInvitationService {

    @Autowired
    private PartnerInvitationMapper invitationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageService messageService;

    @Override
    public PartnerInvitation sendInvitation(String applicantUserName, PartnerInvitationDTO dto) {
        // 先用手机号查找被邀请人
        QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", dto.getInviteePhone());
        User invitee = userMapper.selectOne(phoneWrapper);

        if (invitee == null) {
            throw new RuntimeException("未找到被邀请人信息，请确认手机号正确");
        }

        // 不能邀请自己
        if (invitee.getUserName().equals(applicantUserName)) {
            throw new RuntimeException("不能选择自己作为联合贷款人");
        }

        // 查找申请人用户信息
        QueryWrapper<User> applicantWrapper = new QueryWrapper<>();
        applicantWrapper.eq("user_name", applicantUserName);
        User applicant = userMapper.selectOne(applicantWrapper);

        PartnerInvitation invitation = new PartnerInvitation();
        invitation.setApplicantUserName(applicantUserName);
        invitation.setApplicantRealName(applicant != null ? applicant.getRealName() : "");
        invitation.setApplicantPhone(applicant != null ? applicant.getPhone() : "");
        invitation.setInviteeUserName(invitee.getUserName());
        invitation.setInviteeRealName(invitee.getRealName());
        invitation.setInviteePhone(invitee.getPhone());
        invitation.setProjectDescription(dto.getProjectDescription());
        invitation.setStatus(0); // 待处理

        LocalDateTime now = LocalDateTime.now();
        invitation.setCreateTime(now);
        invitation.setUpdateTime(now);

        invitationMapper.insert(invitation);
        log.info("联合贷款人邀请已发送: applicant={}, invitee={}", applicantUserName, invitee.getUserName());

        // 发送通知给被邀请人
        String title = "联合贷款邀请";
        String content = String.format("农户 %s 邀请您作为联合贷款人，项目描述：%s",
            applicant != null ? applicant.getRealName() : applicantUserName,
            dto.getProjectDescription());
        notificationService.send(invitee.getUserName(), title, content, "partner_invitation", invitation.getId());

        // 发送站内消息
        String msgContent = String.format("您好，%s 邀请您作为联合贷款人共同申请融资。项目描述：%s。请前往「联合贷款邀请」查看并选择同意或拒绝。",
            applicant != null ? applicant.getRealName() : applicantUserName,
            dto.getProjectDescription());
        messageService.sendMessage(applicantUserName, invitee.getUserName(), msgContent);

        return invitation;
    }

    @Override
    public List<InvitationVO> getReceivedInvitations(String userName) {
        QueryWrapper<PartnerInvitation> wrapper = new QueryWrapper<>();
        wrapper.eq("invitee_user_name", userName);
        wrapper.orderByDesc("create_time");

        List<PartnerInvitation> invitations = invitationMapper.selectList(wrapper);
        List<InvitationVO> result = new ArrayList<>();

        for (PartnerInvitation inv : invitations) {
            InvitationVO vo = new InvitationVO();
            vo.setId(inv.getId());
            vo.setApplicantRealName(inv.getApplicantRealName());
            vo.setApplicantPhone(inv.getApplicantPhone());
            vo.setInviteeRealName(inv.getInviteeRealName());
            vo.setInviteePhone(inv.getInviteePhone());
            vo.setProjectDescription(inv.getProjectDescription());
            vo.setStatus(inv.getStatus());
            vo.setCreateTime(inv.getCreateTime() != null ? inv.getCreateTime().toString().substring(0, 16) : "");
            vo.setReplyTime(inv.getReplyTime() != null ? inv.getReplyTime().toString().substring(0, 16) : "");
            vo.setReplyRemark(inv.getReplyRemark());
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<InvitationVO> getSentInvitations(String userName) {
        QueryWrapper<PartnerInvitation> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_user_name", userName);
        wrapper.orderByDesc("create_time");

        List<PartnerInvitation> invitations = invitationMapper.selectList(wrapper);
        List<InvitationVO> result = new ArrayList<>();

        for (PartnerInvitation inv : invitations) {
            InvitationVO vo = new InvitationVO();
            vo.setId(inv.getId());
            vo.setInviteeRealName(inv.getInviteeRealName());
            vo.setInviteePhone(inv.getInviteePhone());
            vo.setApplicantRealName(inv.getInviteeRealName()); // 兼容旧字段
            vo.setApplicantPhone(inv.getInviteePhone());
            vo.setProjectDescription(inv.getProjectDescription());
            vo.setStatus(inv.getStatus());
            vo.setCreateTime(inv.getCreateTime() != null ? inv.getCreateTime().toString().substring(0, 16) : "");
            vo.setReplyTime(inv.getReplyTime() != null ? inv.getReplyTime().toString().substring(0, 16) : "");
            vo.setReplyRemark(inv.getReplyRemark());
            result.add(vo);
        }

        return result;
    }

    @Override
    public boolean replyInvitation(Integer invitationId, Integer status, String remark) {
        PartnerInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            return false;
        }

        invitation.setStatus(status);
        invitation.setReplyRemark(remark);
        invitation.setReplyTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());

        invitationMapper.updateById(invitation);
        log.info("联合贷款人邀请已回复: invitationId={}, status={}", invitationId, status);

        // 发送通知给申请人
        String statusText = status == 1 ? "同意" : "拒绝";
        String title = "联合贷款邀请已被" + statusText;
        String content = String.format("您发送给 %s 的联合贷款邀请已被%s",
            invitation.getInviteeRealName(), statusText);
        if (remark != null && !remark.isEmpty()) {
            content += "，备注：" + remark;
        }
        notificationService.send(invitation.getApplicantUserName(), title, content, "partner_reply", invitationId);

        // 发送站内消息
        String msgContent = String.format("您好，%s 已%s您的联合贷款邀请", invitation.getInviteeRealName(), statusText);
        if (remark != null && !remark.isEmpty()) {
            msgContent += "，备注：" + remark;
        }
        messageService.sendMessage(invitation.getInviteeUserName(), invitation.getApplicantUserName(), msgContent);

        return true;
    }

    @Override
    public PartnerInvitation getById(Integer invitationId) {
        return invitationMapper.selectById(invitationId);
    }

    @Override
    public int clearReceivedInvitations(String userName) {
        QueryWrapper<PartnerInvitation> wrapper = new QueryWrapper<>();
        wrapper.eq("invitee_user_name", userName);
        int deleted = invitationMapper.delete(wrapper);
        log.info("已清除收到的邀请: userName={}, count={}", userName, deleted);
        return deleted;
    }

    @Override
    public int clearSentInvitations(String userName) {
        QueryWrapper<PartnerInvitation> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_user_name", userName);
        int deleted = invitationMapper.delete(wrapper);
        log.info("已清除发送的邀请: userName={}, count={}", userName, deleted);
        return deleted;
    }

    @Override
    public long countAgreedPartners(String userName) {
        QueryWrapper<PartnerInvitation> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_user_name", userName);
        wrapper.eq("status", 1); // 1-已同意
        return invitationMapper.selectCount(wrapper);
    }
}
