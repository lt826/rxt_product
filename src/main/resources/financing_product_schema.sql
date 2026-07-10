-- 融资产品表
CREATE TABLE IF NOT EXISTS tb_financing_product (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '产品ID',
    bank_id INT NOT NULL COMMENT '银行ID',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    min_quota DECIMAL(12,2) NOT NULL COMMENT '最低额度',
    max_quota DECIMAL(12,2) NOT NULL COMMENT '最高额度',
    rate DECIMAL(5,2) NOT NULL COMMENT '年利率（%）',
    min_term INT NOT NULL COMMENT '最短期限（月）',
    max_term INT NOT NULL COMMENT '最长期限（月）',
    description TEXT COMMENT '产品介绍',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_bank (bank_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资产品表';

-- 插入示例融资产品数据
INSERT INTO tb_financing_product (bank_id, name, min_quota, max_quota, rate, min_term, max_term, description, contact_phone, status) VALUES
(1, '春耕贷', 10000, 500000, 4.5, 6, 24, '专为春耕生产设计的贷款产品，支持种子、化肥、农机等采购', '400-123-4567', 1),
(1, '秋收贷', 20000, 800000, 5.0, 3, 18, '秋季丰收专项贷款，额度更高，期限更灵活', '400-123-4567', 1),
(1, '设施农业贷', 50000, 1000000, 4.8, 12, 36, '支持温室大棚、灌溉设施等现代农业设施建设', '400-123-4567', 1),
(1, '农机购置贷', 30000, 600000, 4.2, 12, 48, '专项用于农业机械设备购置，优惠利率支持', '400-123-4567', 1);
