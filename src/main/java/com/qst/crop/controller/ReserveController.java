package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Reserve;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.ReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专家预约：农户申请出诊、专家处理预约
 */
@RestController
@RequestMapping("/api/v1/reserve")
public class ReserveController {

    @Autowired
    private ReserveService reserveService;

    /** 农户发起专家出诊预约 */
    @PostMapping("/apply")
    public Result<Reserve> apply(@RequestBody Reserve reserve) {
        reserve.setQuestioner(SecurityUtil.getCurrentUsername());
        Reserve saved = reserveService.apply(reserve);
        return Result.success("出诊预约已提交，等待专家确认排期", saved);
    }

    /** 农户查看自己的预约 */
    @GetMapping("/my")
    public Result<List<Reserve>> myReserves() {
        String user = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", reserveService.listByQuestioner(user));
    }

    /** 专家查看提问给自己的预约 */
    @GetMapping("/list")
    public Result<List<Reserve>> listForExpert() {
        String expert = SecurityUtil.getCurrentUsername();
        return Result.success("查询成功", reserveService.listForExpert(expert));
    }

    /** 专家处理预约（回复意向 + 状态：1接受/2拒绝） */
    @PutMapping("/handle")
    public Result<Reserve> handle(@RequestBody Reserve body) {
        String expert = SecurityUtil.getCurrentUsername();
        Reserve updated = reserveService.handle(body.getId(), expert, body.getAnswer(), body.getStatus());
        return Result.success("处理成功", updated);
    }
}
