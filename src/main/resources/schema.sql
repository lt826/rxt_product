-- =============================================================
-- 融销通 数据库建表脚本
-- 基于数字经济的农产品融销一体平台
-- 数据库: MySQL
-- =============================================================

CREATE DATABASE IF NOT EXISTS rxt DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rxt;

-- ==============================================
-- 1. 用户表
-- ==============================================
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
    user_name   VARCHAR(50)  NOT NULL COMMENT '用户名（主键）',
    password    VARCHAR(200) NOT NULL COMMENT '密码',
    nick_name   VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    identity_num VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    address     VARCHAR(200) DEFAULT NULL COMMENT '地址',
    role        VARCHAR(20)  DEFAULT NULL COMMENT '角色(farmer/buyer/bank/expert/admin)',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    integral    INT          DEFAULT 0 COMMENT '积分',
    credit      INT          DEFAULT 0 COMMENT '信誉',
    avatar      VARCHAR(500) DEFAULT NULL COMMENT '头像',
    real_name   VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    PRIMARY KEY (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==============================================
-- 2. 专家信息表
-- ==============================================
DROP TABLE IF EXISTS tb_expert;
CREATE TABLE tb_expert (
    user_name   VARCHAR(50)  NOT NULL COMMENT '用户名（主键）',
    real_name   VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    profession  VARCHAR(200) DEFAULT NULL COMMENT '从事专业',
    position    VARCHAR(100) DEFAULT NULL COMMENT '职位',
    belong      VARCHAR(200) DEFAULT NULL COMMENT '所属单位',
    PRIMARY KEY (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家信息表';

-- ==============================================
-- 3. 在线问答表
-- ==============================================
DROP TABLE IF EXISTS tb_question;
CREATE TABLE tb_question (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT 'id',
    expert_name VARCHAR(50)  DEFAULT NULL COMMENT '专家用户名',
    questioner  VARCHAR(50)  DEFAULT NULL COMMENT '咨询者用户名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    plant_name  VARCHAR(100) DEFAULT NULL COMMENT '农作物名称',
    title       VARCHAR(200) DEFAULT NULL COMMENT '问题标题',
    question    TEXT         DEFAULT NULL COMMENT '问题描述',
    answer      TEXT         DEFAULT NULL COMMENT '专家回答',
    status      INT          DEFAULT 0 COMMENT '状态(0:提问中,1:回答完毕)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线问答表';

CREATE TABLE tb_question_message (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '消息id',
    question_id INT          NOT NULL COMMENT '所属问题id',
    sender      VARCHAR(50)  NOT NULL COMMENT '发送者用户名',
    sender_role VARCHAR(20)  DEFAULT NULL COMMENT '发送者角色(farmer/expert)',
    content     TEXT         NOT NULL COMMENT '消息内容',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_question (question_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答对话消息表';

CREATE TABLE tb_follow (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT 'id',
    follower    VARCHAR(50)  NOT NULL COMMENT '关注者(买家用户名)',
    followee    VARCHAR(50)  NOT NULL COMMENT '被关注者(农户用户名)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow (follower, followee),
    INDEX idx_followee (followee)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- ==============================================
-- 4. 专家预约表
-- ==============================================
DROP TABLE IF EXISTS tb_reserve;
CREATE TABLE tb_reserve (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT 'id',
    expert_name     VARCHAR(50)  DEFAULT NULL COMMENT '专家用户名',
    questioner      VARCHAR(50)  DEFAULT NULL COMMENT '咨询者用户名',
    area            VARCHAR(50)  DEFAULT NULL COMMENT '面积',
    address         VARCHAR(500) DEFAULT NULL COMMENT '土地地址',
    plant_name      VARCHAR(100) DEFAULT NULL COMMENT '农作物名称',
    soil_condition  VARCHAR(200) DEFAULT NULL COMMENT '土壤条件',
    plant_condition VARCHAR(200) DEFAULT NULL COMMENT '作物条件',
    plant_detail    TEXT         DEFAULT NULL COMMENT '作物详细信息',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    message         TEXT         DEFAULT NULL COMMENT '买家/农户留言',
    answer          TEXT         DEFAULT NULL COMMENT '专家回复意向',
    status          INT          DEFAULT 0 COMMENT '状态(0:申请中,1:已接受,2:已拒绝)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家预约表';

-- ==============================================
-- 5. 地址表
-- ==============================================
DROP TABLE IF EXISTS tb_address;
CREATE TABLE tb_address (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT '地址id',
    own_name        VARCHAR(50)  DEFAULT NULL COMMENT '用户名',
    consignee       VARCHAR(100) DEFAULT NULL COMMENT '收货人',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    address_detail  VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
    is_default      INT          DEFAULT 0 COMMENT '默认地址(0:不默认,1:默认)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

-- ==============================================
-- 6. 银行产品表
-- ==============================================
DROP TABLE IF EXISTS tb_bank;
CREATE TABLE tb_bank (
    bank_id     INT          NOT NULL AUTO_INCREMENT COMMENT '银行id',
    bank_name   VARCHAR(100) DEFAULT NULL COMMENT '银行名',
    introduce   TEXT         DEFAULT NULL COMMENT '银行介绍',
    bank_phone  VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    money       DECIMAL(12,2) DEFAULT NULL COMMENT '融资金额',
    rate        DECIMAL(5,2) DEFAULT NULL COMMENT '年利率',
    repayment   INT          DEFAULT NULL COMMENT '还款期限(月)',
    PRIMARY KEY (bank_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行产品表';

-- ==============================================
-- 7. 银行用户表
-- ==============================================
DROP TABLE IF EXISTS tb_bank_user;
CREATE TABLE tb_bank_user (
    user_name   VARCHAR(50)  NOT NULL COMMENT '用户名（主键）',
    bank_id     INT          DEFAULT NULL COMMENT '银行id',
    password    VARCHAR(200) DEFAULT NULL COMMENT '密码',
    real_name   VARCHAR(100) DEFAULT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role        VARCHAR(20)  DEFAULT NULL COMMENT '角色',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行用户表';

-- ==============================================
-- 8. 知识讨论表
-- ==============================================
DROP TABLE IF EXISTS tb_discuss;
CREATE TABLE tb_discuss (
    discuss_id   INT          NOT NULL AUTO_INCREMENT COMMENT '讨论id',
    knowledge_id INT          DEFAULT NULL COMMENT '知识id',
    content      TEXT         DEFAULT NULL COMMENT '讨论内容',
    own_name     VARCHAR(50)  DEFAULT NULL COMMENT '发布者名字',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (discuss_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识讨论表';

-- ==============================================
-- 9. 融资申请表
-- ==============================================
DROP TABLE IF EXISTS tb_finance;
CREATE TABLE tb_finance (
    finance_id         INT          NOT NULL AUTO_INCREMENT COMMENT 'id',
    bank_id            INT          DEFAULT NULL COMMENT '银行id',
    own_name           VARCHAR(50)  DEFAULT NULL COMMENT '用户名',
    real_name          VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    phone              VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    id_num             VARCHAR(20)  DEFAULT NULL COMMENT '身份证号',
    status             INT          DEFAULT 0 COMMENT '状态(0:申请中,1:驳回,2:申请通过)',
    remark             VARCHAR(500) DEFAULT NULL COMMENT '备注',
    money              DECIMAL(12,2) DEFAULT NULL COMMENT '融资金额',
    rate               DECIMAL(5,2) DEFAULT NULL COMMENT '年利率',
    repayment          INT          DEFAULT NULL COMMENT '还款期限(月)',
    create_time        DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time        DATETIME     DEFAULT NULL COMMENT '更新时间',
    combination_name1  VARCHAR(100) DEFAULT NULL COMMENT '联合贷款人1姓名',
    combination_phone1 VARCHAR(20)  DEFAULT NULL COMMENT '联合贷款人1电话',
    combination_idnum1 VARCHAR(20)  DEFAULT NULL COMMENT '联合贷款人1身份证',
    combination_name2  VARCHAR(100) DEFAULT NULL COMMENT '联合贷款人2姓名',
    combination_phone2 VARCHAR(20)  DEFAULT NULL COMMENT '联合贷款人2电话',
    combination_idnum2 VARCHAR(20)  DEFAULT NULL COMMENT '联合贷款人2身份证',
    file_info          VARCHAR(500) DEFAULT NULL COMMENT '资料路径',
    PRIMARY KEY (finance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资申请表';

-- ==============================================
-- 10. 融资意向表
-- ==============================================
DROP TABLE IF EXISTS tb_financing_intention;
CREATE TABLE tb_financing_intention (
    id               INT          NOT NULL AUTO_INCREMENT COMMENT '意向id',
    user_name        VARCHAR(50)  DEFAULT NULL COMMENT '用户名',
    real_name        VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    address          VARCHAR(500) DEFAULT NULL COMMENT '地址',
    amount           INT          DEFAULT NULL COMMENT '融资金额',
    application      VARCHAR(500) DEFAULT NULL COMMENT '融资用途',
    item             VARCHAR(200) DEFAULT NULL COMMENT '融资农作物',
    repayment_period INT          DEFAULT NULL COMMENT '还款日期',
    area             INT          DEFAULT NULL COMMENT '种植面积',
    phone            VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    create_time      DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time      DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='融资意向表';

-- ==============================================
-- 11. 商品/供求信息表
-- ==============================================
DROP TABLE IF EXISTS tb_goods;
CREATE TABLE tb_goods (
    goods_id        INT          NOT NULL AUTO_INCREMENT COMMENT '商品/供求id',
    title           VARCHAR(200) DEFAULT NULL COMMENT '标题',
    price           DECIMAL(10,2) DEFAULT NULL COMMENT '期望价格',
    content         TEXT         DEFAULT NULL COMMENT '内容详情',
    goods_status    INT          DEFAULT NULL COMMENT '状态',
    type            VARCHAR(10)  DEFAULT NULL COMMENT '类型(1:销售供应,2:采购需求)',
    picture         VARCHAR(500) DEFAULT NULL COMMENT '图片',
    own_name        VARCHAR(50)  DEFAULT NULL COMMENT '发布者',
    cooperation_name VARCHAR(100) DEFAULT NULL COMMENT '合作人名字',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    address         VARCHAR(500) DEFAULT NULL COMMENT '交易/产地地址',
    PRIMARY KEY (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品/供求信息表';

-- ==============================================
-- 12. 农业知识表
-- ==============================================
DROP TABLE IF EXISTS tb_knowledge;
CREATE TABLE tb_knowledge (
    knowledge_id INT          NOT NULL AUTO_INCREMENT COMMENT '知识id',
    title        VARCHAR(200) DEFAULT NULL COMMENT '标题',
    content      TEXT         DEFAULT NULL COMMENT '内容',
    pic_path     VARCHAR(500) DEFAULT NULL COMMENT '视频图片路径',
    own_name     VARCHAR(50)  DEFAULT NULL COMMENT '发布者名字',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农业知识表';

-- ==============================================
-- 13. 买家交易订单表
-- ==============================================
DROP TABLE IF EXISTS tb_purchase;
CREATE TABLE tb_purchase (
    purchase_id    INT          NOT NULL AUTO_INCREMENT COMMENT '订单id',
    own_name       VARCHAR(50)  DEFAULT NULL COMMENT '买家用户名',
    purchase_type  INT          DEFAULT NULL COMMENT '类型(1:买到的,2:卖出的)',
    total_price    DECIMAL(12,2) DEFAULT NULL COMMENT '总价格',
    address        VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
    consignee      VARCHAR(100) DEFAULT NULL COMMENT '收货人姓名',
    consignee_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    purchase_status INT         DEFAULT NULL COMMENT '订单状态(1:待付款,2:待发货,3:待收货,4:已收货)',
    create_time    DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (purchase_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家交易订单表';

-- ==============================================
-- 14. 订单详情表
-- ==============================================
DROP TABLE IF EXISTS tb_purchase_detail;
CREATE TABLE tb_purchase_detail (
    detail_id   INT          NOT NULL AUTO_INCREMENT COMMENT '明细id',
    purchase_id INT          DEFAULT NULL COMMENT '订单id',
    goods_id    INT          DEFAULT NULL COMMENT '商品id',
    unin_price  DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    sum_price   DECIMAL(12,2) DEFAULT NULL COMMENT '总价格',
    count       INT          DEFAULT NULL COMMENT '数量',
    PRIMARY KEY (detail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- ==============================================
-- 15. 卖家出货订单表
-- ==============================================
DROP TABLE IF EXISTS tb_sell_purchase;
CREATE TABLE tb_sell_purchase (
    sell_purchase_id INT          NOT NULL AUTO_INCREMENT COMMENT '卖出订单id',
    purchase_id      INT          DEFAULT NULL COMMENT '全局订单id',
    own_name         VARCHAR(50)  DEFAULT NULL COMMENT '卖家用户名',
    purchase_type    INT          DEFAULT NULL COMMENT '类型(2:卖出的)',
    unin_price       DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    sum_price        DECIMAL(12,2) DEFAULT NULL COMMENT '总价格',
    address          VARCHAR(500) DEFAULT NULL COMMENT '发货地址',
    ship_name        VARCHAR(100) DEFAULT NULL COMMENT '发货人姓名',
    ship_phone       VARCHAR(20)  DEFAULT NULL COMMENT '发货人电话',
    purchase_status  INT          DEFAULT NULL COMMENT '订单状态(1:待付款,2:待发货,3:待收货,4:已收货)',
    create_time      DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time      DATETIME     DEFAULT NULL COMMENT '更新时间',
    goods_id         INT          DEFAULT NULL COMMENT '商品id',
    PRIMARY KEY (sell_purchase_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卖家出货订单表';

-- ==============================================
-- 16. 购物车表
-- ==============================================
DROP TABLE IF EXISTS tb_shoppingcart;
CREATE TABLE tb_shoppingcart (
    shopping_id INT      NOT NULL AUTO_INCREMENT COMMENT '购物车id',
    goods_id    INT      DEFAULT NULL COMMENT '商品id',
    count       INT      DEFAULT NULL COMMENT '数量',
    own_name    VARCHAR(50) DEFAULT NULL COMMENT '用户',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (shopping_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ==============================================
-- 17. 买家-农户消息表
-- ==============================================
DROP TABLE IF EXISTS tb_message;
CREATE TABLE tb_message (
    message_id  INT          NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    from_user   VARCHAR(50)  NOT NULL COMMENT '发送方用户名',
    to_user     VARCHAR(50)  NOT NULL COMMENT '接收方用户名',
    content     TEXT         NOT NULL COMMENT '消息内容',
    is_read     INT          DEFAULT 0 COMMENT '是否已读(0:未读,1:已读)',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (message_id),
    INDEX idx_from_to (from_user, to_user, create_time),
    INDEX idx_to_read (to_user, is_read, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家-农户消息表';

-- ==============================================
-- 18. 联合贷款人邀请表
-- 注意：本脚本含 DROP 语句仅作结构定义参考。实际建表由
--       DatabaseSchemaInitializer 在启动时以 CREATE TABLE IF NOT EXISTS
--       幂等执行，不会清空已有数据。
-- ==============================================
DROP TABLE IF EXISTS tb_partner_invitation;
CREATE TABLE tb_partner_invitation (
    id                  INT          NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
    applicant_user_name VARCHAR(50)  NOT NULL COMMENT '申请人用户名',
    applicant_real_name VARCHAR(50)  DEFAULT '' COMMENT '申请人真实姓名',
    applicant_phone     VARCHAR(20)  DEFAULT '' COMMENT '申请人电话',
    invitee_user_name   VARCHAR(50)  NOT NULL COMMENT '被邀请人用户名',
    invitee_real_name   VARCHAR(50)  DEFAULT '' COMMENT '被邀请人真实姓名',
    invitee_phone       VARCHAR(20)  DEFAULT '' COMMENT '被邀请人电话',
    project_description TEXT         DEFAULT NULL COMMENT '融资项目描述',
    status              INT          NOT NULL DEFAULT 0 COMMENT '状态(0:待处理,1:已同意,2:已拒绝)',
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    reply_time          DATETIME     DEFAULT NULL COMMENT '回复时间',
    reply_remark        VARCHAR(255) DEFAULT NULL COMMENT '回复备注',
    PRIMARY KEY (id),
    INDEX idx_applicant (applicant_user_name),
    INDEX idx_invitee (invitee_user_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联合贷款人邀请表';

-- ==============================================
-- 19. 站内通知表
-- ==============================================
DROP TABLE IF EXISTS tb_notification;
CREATE TABLE tb_notification (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    receiver    VARCHAR(50)  NOT NULL COMMENT '接收人用户名',
    title       VARCHAR(100) DEFAULT '' COMMENT '标题',
    content     TEXT         DEFAULT NULL COMMENT '内容',
    type        VARCHAR(50)  DEFAULT '' COMMENT '类型',
    is_read     INT          DEFAULT 0 COMMENT '是否已读(0:未读,1:已读)',
    related_id  INT          DEFAULT NULL COMMENT '关联ID',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_receiver (receiver),
    INDEX idx_read (receiver, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

CREATE TABLE tb_match_rule (
    id                 INT          NOT NULL AUTO_INCREMENT COMMENT 'id',
    partner_weight     DECIMAL(5,2) NOT NULL DEFAULT 20.00  COMMENT '联合贷款人权重(%)',
    credit_weight      DECIMAL(5,2) NOT NULL DEFAULT 30.00  COMMENT '信誉分权重(%)',
    amount_weight      DECIMAL(5,2) NOT NULL DEFAULT 25.00  COMMENT '申请金额权重(%)',
    term_weight        DECIMAL(5,2) NOT NULL DEFAULT 25.00  COMMENT '期限权重(%)',
    amount_upper_bound DECIMAL(12,2) NOT NULL DEFAULT 500000.00 COMMENT '金额归一化上限',
    update_time        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配规则权重配置表';

-- ==============================================
-- 20. 首页精选内容表
-- ==============================================
DROP TABLE IF EXISTS tb_homepage_feature;
CREATE TABLE tb_homepage_feature (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    section_key VARCHAR(32)  NOT NULL COMMENT '板块: finance/expert/answer/knowledge/goods',
    ref_id      VARCHAR(64)  NOT NULL COMMENT '关联主键(专家为user_name,其余为数字id)',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '展示顺序,升序',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_section_ref (section_key, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页各板块精选内容';