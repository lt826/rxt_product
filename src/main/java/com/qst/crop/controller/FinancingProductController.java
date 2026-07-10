package com.qst.crop.controller;

import com.qst.crop.common.Result;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.model.ProductDTO;
import com.qst.crop.service.FinancingProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 融资产品控制器
 */
@RestController
@RequestMapping("/api/v1/financing-product")
public class FinancingProductController {

    @Autowired
    private FinancingProductService financingProductService;

    /**
     * 创建融资产品
     */
    @PostMapping("/create")
    public Result<FinancingProduct> create(Authentication auth, @RequestBody ProductDTO dto) {
        // 从认证信息中获取银行ID（实际项目中应该从用户信息中获取）
        Integer bankId = 1; // 临时写死，实际应从 auth 中获取
        FinancingProduct product = financingProductService.create(bankId, dto);
        return Result.success("产品创建成功", product);
    }

    /**
     * 更新融资产品
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        boolean ok = financingProductService.update(id, dto);
        return ok ? Result.success("更新成功", true) : Result.error(404, "产品不存在");
    }

    /**
     * 删除融资产品
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean ok = financingProductService.delete(id);
        return ok ? Result.success("删除成功", true) : Result.error(404, "产品不存在");
    }

    /**
     * 获取所有融资产品
     */
    @GetMapping("/list")
    public Result<List<FinancingProduct>> getAllProducts() {
        List<FinancingProduct> list = financingProductService.getAllProducts();
        return Result.success("查询成功", list);
    }

    /**
     * 获取银行的融资产品
     */
    @GetMapping("/bank/{bankId}")
    public Result<List<FinancingProduct>> getBankProducts(@PathVariable Integer bankId) {
        List<FinancingProduct> list = financingProductService.getBankProducts(bankId);
        return Result.success("查询成功", list);
    }

    /**
     * 启用/停用产品
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Integer id, @RequestBody Integer status) {
        boolean ok = financingProductService.updateStatus(id, status);
        return ok ? Result.success("状态更新成功", true) : Result.error(404, "产品不存在");
    }
}
