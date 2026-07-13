-- 求购需求接取记录表
-- 用途：农户在「求购大厅」接取买家/其他农户发布的求购需求(type=2 的 tb_goods)时，记录一条接取记录。
-- 执行：在本机 MySQL 的 rxt 库执行一次即可（root/123456）。

CREATE TABLE IF NOT EXISTS tb_demand_claim (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '接取记录id',
  demand_id BIGINT NOT NULL COMMENT '求购需求id(关联 tb_goods.goods_id, type=2)',
  claimer_name VARCHAR(50) NOT NULL COMMENT '接取农户用户名',
  demand_owner_name VARCHAR(50) NOT NULL COMMENT '需求发布者用户名(冗余,便于发布者查询)',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待响应 1已同意 2已拒绝',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_demand_claimer (demand_id, claimer_name) COMMENT '同一农户对同一需求只能接取一次',
  INDEX idx_owner (demand_owner_name) COMMENT '发布者查询自己需求被谁接取',
  INDEX idx_claimer (claimer_name) COMMENT '农户查询自己接取过哪些需求'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求购需求接取记录';
