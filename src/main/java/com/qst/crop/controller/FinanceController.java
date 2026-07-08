package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.Finance;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.RecommendPartnerVO;
import com.qst.crop.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @GetMapping("/recommend-partners")
    public Result<List<RecommendPartnerVO>> recommendPartners(
            @RequestParam Integer amount,
            @RequestParam String item) {
        List<RecommendPartnerVO> list = financeService.recommendPartners(amount, item);
        return Result.success("推荐成功", list);
    }

    @PostMapping("/apply")
    public Result<Finance> apply(Authentication auth, @RequestBody FinanceApplyDTO dto) {
        String userName = auth.getName();
        Finance finance = financeService.apply(userName, dto);
        return Result.success("申请已提交，等待银行审核", finance);
    }

    @GetMapping("/list")
    public Result<List<Finance>> getMyFinances(Authentication auth) {
        String userName = auth.getName();
        List<Finance> list = financeService.getMyFinances(userName);
        return Result.success("查询成功", list);
    }

    @GetMapping("/{financeId}")
    public Result<Finance> getFinanceDetail(@PathVariable Integer financeId) {
        Finance finance = financeService.getFinanceById(financeId);
        if (finance == null) {
            return Result.error(404, "融资申请不存在");
        }
        return Result.success("查询成功", finance);
    }
}
