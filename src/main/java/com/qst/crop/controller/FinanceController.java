package com.qst.crop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.common.Result;
import com.qst.crop.entity.Bank;
import com.qst.crop.entity.Finance;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.model.FinanceApplyDTO;
import com.qst.crop.model.RecommendPartnerVO;
import com.qst.crop.dao.BankMapper;
import com.qst.crop.dao.FinancingProductMapper;
import com.qst.crop.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private FinancingProductMapper financingProductMapper;

    @GetMapping("/recommend-partners")
    public Result<List<RecommendPartnerVO>> recommendPartners(
            Authentication auth,
            @RequestParam Integer amount,
            @RequestParam String item) {
        String currentUserName = auth.getName();
        List<RecommendPartnerVO> list = financeService.recommendPartners(currentUserName, amount, item);
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

    @GetMapping("/bank/list")
    public Result<List<Finance>> getAllFinances() {
        return Result.success("查询成功", financeService.getAllFinances());
    }

    /**
     * 获取银行融资产品列表（农户端工作台展示可用融资产品）
     */
    @GetMapping("/products")
    public Result<List<FinancingProduct>> getFinancingProducts() {
        QueryWrapper<FinancingProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只返回启用的产品
        wrapper.orderByDesc("create_time");
        List<FinancingProduct> products = financingProductMapper.selectList(wrapper);
        return Result.success("查询成功", products);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String remark = (String) body.getOrDefault("remark", "");
        boolean ok = financeService.updateFinanceStatus(id, status, remark);
        return ok ? Result.success("操作成功", true) : Result.error(404, "融资申请不存在");
    }
}
