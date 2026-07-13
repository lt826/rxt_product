package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.DemandClaim;
import com.qst.crop.security.SecurityUtil;
import com.qst.crop.service.DemandClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 求购需求接取控制器
 */
@RestController
@RequestMapping("/api/v1/demand-claim")
public class DemandClaimController {

    @Autowired
    private DemandClaimService demandClaimService;

    /** 农户接取一条求购需求 */
    @PostMapping("/claim")
    public Result<DemandClaim> claim(@RequestBody Map<String, Object> body) {
        String claimer = SecurityUtil.getCurrentUsername();
        if (claimer == null) {
            return Result.error(401, "未登录");
        }
        Object demandIdObj = body.get("demandId");
        if (demandIdObj == null) {
            return Result.error(400, "缺少 demandId");
        }
        Integer demandId;
        try {
            demandId = Integer.valueOf(String.valueOf(demandIdObj));
        } catch (NumberFormatException e) {
            return Result.error(400, "demandId 非法");
        }
        try {
            DemandClaim claim = demandClaimService.claim(claimer, demandId);
            return Result.success("接取成功", claim);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 查询某条求购需求的所有接取者（发布者查看用） */
    @GetMapping("/by-demand/{demandId}")
    public Result<List<DemandClaim>> listByDemand(@PathVariable Integer demandId) {
        return Result.success("查询成功", demandClaimService.listByDemand(demandId));
    }

    /** 查询当前用户已接取的需求记录（农户在求购大厅标"已接取"用） */
    @GetMapping("/my-claims")
    public Result<List<DemandClaim>> myClaims() {
        String user = SecurityUtil.getCurrentUsername();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success("查询成功", demandClaimService.listMyClaims(user));
    }
}
