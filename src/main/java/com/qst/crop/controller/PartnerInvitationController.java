package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.PartnerInvitation;
import com.qst.crop.model.InvitationVO;
import com.qst.crop.model.PartnerInvitationDTO;
import com.qst.crop.service.PartnerInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 联合贷款人邀请控制器
 */
@RestController
@RequestMapping("/api/v1/partner-invitation")
public class PartnerInvitationController {

    @Autowired
    private PartnerInvitationService invitationService;

    /**
     * 发送邀请
     */
    @PostMapping("/send")
    public Result<PartnerInvitation> sendInvitation(Authentication auth, @RequestBody PartnerInvitationDTO dto) {
        try {
            String userName = auth.getName();
            PartnerInvitation invitation = invitationService.sendInvitation(userName, dto);
            return Result.success("邀请已发送", invitation);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取我收到的邀请
     */
    @GetMapping("/received")
    public Result<List<InvitationVO>> getReceivedInvitations(Authentication auth) {
        String userName = auth.getName();
        List<InvitationVO> list = invitationService.getReceivedInvitations(userName);
        return Result.success("查询成功", list);
    }

    /**
     * 获取我发送的邀请
     */
    @GetMapping("/sent")
    public Result<List<InvitationVO>> getSentInvitations(Authentication auth) {
        String userName = auth.getName();
        List<InvitationVO> list = invitationService.getSentInvitations(userName);
        return Result.success("查询成功", list);
    }

    /**
     * 清除我收到的全部邀请
     */
    @DeleteMapping("/received")
    public Result<Integer> clearReceivedInvitations(Authentication auth) {
        int count = invitationService.clearReceivedInvitations(auth.getName());
        return Result.success("已清除 " + count + " 条收到的邀请", count);
    }

    /**
     * 清除我发送的全部邀请
     */
    @DeleteMapping("/sent")
    public Result<Integer> clearSentInvitations(Authentication auth) {
        int count = invitationService.clearSentInvitations(auth.getName());
        return Result.success("已清除 " + count + " 条发送的邀请", count);
    }

    /**
     * 回复邀请
     */
    @PutMapping("/{id}/reply")
    public Result<Boolean> replyInvitation(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body
    ) {
        Integer status = (Integer) body.get("status");
        String remark = (String) body.getOrDefault("remark", "");

        boolean ok = invitationService.replyInvitation(id, status, remark);
        return ok ? Result.success("回复成功", true) : Result.error(404, "邀请不存在");
    }

    /**
     * 获取邀请详情
     */
    @GetMapping("/{id}")
    public Result<PartnerInvitation> getDetail(@PathVariable Integer id) {
        PartnerInvitation invitation = invitationService.getById(id);
        if (invitation == null) {
            return Result.error(404, "邀请不存在");
        }
        return Result.success("查询成功", invitation);
    }
}
