package com.qst.crop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.crop.dao.FinancingProductMapper;
import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.model.ProductDTO;
import com.qst.crop.service.FinancingProductService;
import com.qst.crop.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 融资产品服务实现类
 */
@Slf4j
@Service
public class FinancingProductServiceImpl implements FinancingProductService {

    @Autowired
    private FinancingProductMapper productMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public FinancingProduct create(Integer bankId, ProductDTO dto) {
        FinancingProduct product = new FinancingProduct();
        product.setBankId(bankId);
        product.setName(dto.getName());
        product.setMinQuota(dto.getMinQuota());
        product.setMaxQuota(dto.getMaxQuota());
        product.setRate(dto.getRate());
        product.setMinTerm(dto.getMinTerm());
        product.setMaxTerm(dto.getMaxTerm());
        product.setDescription(dto.getDescription());
        product.setContactPhone(dto.getContactPhone());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        LocalDateTime now = LocalDateTime.now();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);
        log.info("融资产品已创建: bankId={}, productId={}", bankId, product.getId());
        // 通知所有农户有新的融资产品
        try {
            String name = product.getName() != null ? product.getName() : "新融资产品";
            notificationService.sendToRole("farmer", "新增融资产品消息",
                    "有新的融资产品《" + name + "》发布，快去看看是否适合您。", "finance_product_new", product.getId());
        } catch (Exception e) {
            log.warn("融资产品通知发送失败: {}", e.getMessage());
        }
        return product;
    }

    @Override
    public boolean update(Integer id, ProductDTO dto) {
        FinancingProduct product = productMapper.selectById(id);
        if (product == null) {
            return false;
        }
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getMinQuota() != null) product.setMinQuota(dto.getMinQuota());
        if (dto.getMaxQuota() != null) product.setMaxQuota(dto.getMaxQuota());
        if (dto.getRate() != null) product.setRate(dto.getRate());
        if (dto.getMinTerm() != null) product.setMinTerm(dto.getMinTerm());
        if (dto.getMaxTerm() != null) product.setMaxTerm(dto.getMaxTerm());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getContactPhone() != null) product.setContactPhone(dto.getContactPhone());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return true;
    }

    @Override
    public boolean delete(Integer id) {
        int rows = productMapper.deleteById(id);
        return rows > 0;
    }

    @Override
    public List<FinancingProduct> getAllProducts() {
        QueryWrapper<FinancingProduct> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<FinancingProduct> getBankProducts(Integer bankId) {
        QueryWrapper<FinancingProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("bank_id", bankId);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        FinancingProduct product = productMapper.selectById(id);
        if (product == null) {
            return false;
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return true;
    }
}
