-- 联合贷款人邀请表
-- 注意：该表已由后端 DatabaseSchemaInitializer 在启动时自动创建（IF NOT EXISTS），
--       此文件仅作为结构参考，无需手动执行。回复备注列必须为 reply_remark。
CREATE TABLE IF NOT EXISTS tb_partner_invitation (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '邀请ID',
    applicant_user_name VARCHAR(50) NOT NULL COMMENT '申请人用户名',
    applicant_real_name VARCHAR(50) DEFAULT '' COMMENT '申请人真实姓名',
    applicant_phone VARCHAR(20) DEFAULT '' COMMENT '申请人电话',
    invitee_user_name VARCHAR(50) NOT NULL COMMENT '被邀请人用户名',
    invitee_real_name VARCHAR(50) DEFAULT '' COMMENT '被邀请人真实姓名',
    invitee_phone VARCHAR(20) DEFAULT '' COMMENT '被邀请人电话',
    project_description TEXT COMMENT '融资项目描述',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已同意，2-已拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    reply_time DATETIME COMMENT '回复时间',
    reply_remark VARCHAR(255) COMMENT '回复备注',
    INDEX idx_applicant (applicant_user_name),
    INDEX idx_invitee (invitee_user_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联合贷款人邀请表';
