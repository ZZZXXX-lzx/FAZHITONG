-- ============================================================
-- 法智通 Phase 2 · 合同全生命周期 + 电子签章 + 知识产权 增量脚本
-- 使用方法: mysql -u root -p fazhitong < phase2-contract.sql
-- ============================================================
USE fazhitong;

-- 企业合同表新增生命周期与签章字段
ALTER TABLE `enterprise_contract`
    ADD COLUMN `signer_name` VARCHAR(64) NULL COMMENT '签署人' AFTER `status`,
    ADD COLUMN `sign_cert_no` VARCHAR(128) NULL COMMENT '电子签章凭据号' AFTER `signer_name`,
    ADD COLUMN `sign_time` DATETIME NULL COMMENT '签署时间' AFTER `sign_cert_no`,
    ADD COLUMN `archive_time` DATETIME NULL COMMENT '归档时间' AFTER `sign_time`;

-- 知识产权台账表
CREATE TABLE IF NOT EXISTS `ip_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `ip_type` VARCHAR(32) DEFAULT 'TRADEMARK' COMMENT 'TRADEMARK/PATENT/COPYRIGHT',
    `name` VARCHAR(200) NOT NULL COMMENT '名称',
    `register_no` VARCHAR(128) DEFAULT NULL COMMENT '注册/申请号',
    `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING/GRANTED/INVALID',
    `apply_date` DATETIME DEFAULT NULL,
    `expire_date` DATETIME DEFAULT NULL,
    `owner` VARCHAR(128) DEFAULT NULL COMMENT '权利人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise (`enterprise_id`),
    INDEX idx_type (`ip_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识产权台账';
