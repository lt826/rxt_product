-- ==========================================================
-- 订单物流字段迁移脚本
-- 作用：给已存在的 tb_purchase / tb_sell_purchase 补充物流字段
--       (收货人姓名/电话、发货人姓名/电话)
--
-- 注意：DatabaseSchemaInitializer 启动时只会执行 schema.sql 里的
--       CREATE TABLE IF NOT EXISTS，不会执行 ALTER，也不会修改已存在的表。
--       所以如果数据库里这两张表已经存在，需要【手动执行一次】本脚本。
--       全新数据库则无需执行（schema.sql 的 CREATE TABLE 已含这些列）。
-- ==========================================================

-- 买家订单表：补收货人姓名、收货人电话（收货地址 address 已存在）
ALTER TABLE tb_purchase
    ADD COLUMN consignee        VARCHAR(100) DEFAULT NULL COMMENT '收货人姓名' AFTER address,
    ADD COLUMN consignee_phone  VARCHAR(20)  DEFAULT NULL COMMENT '收货人电话' AFTER consignee;

-- 卖家订单表：补发货人姓名、发货人电话（发货地址 address 已存在）
ALTER TABLE tb_sell_purchase
    ADD COLUMN ship_name        VARCHAR(100) DEFAULT NULL COMMENT '发货人姓名' AFTER address,
    ADD COLUMN ship_phone       VARCHAR(20)  DEFAULT NULL COMMENT '发货人电话' AFTER ship_name;
