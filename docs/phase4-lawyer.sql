-- ============================================================
-- 法智通 Phase 4 · 律所数字化管理（案件管理 + 客户管理）增量脚本
-- 使用方法: mysql -u root -p fazhitong < phase4-lawyer.sql
-- ============================================================
USE fazhitong;

-- 律师承办案件台账
CREATE TABLE IF NOT EXISTS `lawyer_case` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lawyer_id` BIGINT NOT NULL COMMENT '承办律师用户ID',
    `case_name` VARCHAR(200) NOT NULL COMMENT '案件名称',
    `client_name` VARCHAR(128) DEFAULT NULL COMMENT '当事人名称',
    `case_type` VARCHAR(128) DEFAULT NULL COMMENT '案由/案件类型',
    `status` VARCHAR(32) DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/CLOSED/ARCHIVED',
    `description` TEXT COMMENT '案情描述',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `close_time` DATETIME DEFAULT NULL COMMENT '结案时间',
    INDEX idx_lawyer (`lawyer_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律师承办案件';

-- 律师客户信息
CREATE TABLE IF NOT EXISTS `lawyer_client` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lawyer_id` BIGINT NOT NULL COMMENT '所属律师用户ID',
    `client_name` VARCHAR(128) NOT NULL COMMENT '客户名称',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `client_type` VARCHAR(32) DEFAULT 'PERSONAL' COMMENT 'PERSONAL/ENTERPRISE',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lawyer (`lawyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律师客户';
