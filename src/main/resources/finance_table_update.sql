-- 更新融资申请表，添加融资农作物字段
ALTER TABLE tb_finance ADD COLUMN IF NOT EXISTS crop_item VARCHAR(200) DEFAULT NULL COMMENT '融资农作物' AFTER file_info;
