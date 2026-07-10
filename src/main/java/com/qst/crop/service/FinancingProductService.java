package com.qst.crop.service;

import com.qst.crop.entity.FinancingProduct;
import com.qst.crop.model.ProductDTO;

import java.util.List;

/**
 * 融资产品服务接口
 */
public interface FinancingProductService {

    /**
     * 创建融资产品
     */
    FinancingProduct create(Integer bankId, ProductDTO dto);

    /**
     * 更新融资产品
     */
    boolean update(Integer id, ProductDTO dto);

    /**
     * 删除融资产品
     */
    boolean delete(Integer id);

    /**
     * 获取所有融资产品
     */
    List<FinancingProduct> getAllProducts();

    /**
     * 获取银行的融资产品
     */
    List<FinancingProduct> getBankProducts(Integer bankId);

    /**
     * 启用/停用产品
     */
    boolean updateStatus(Integer id, Integer status);
}
