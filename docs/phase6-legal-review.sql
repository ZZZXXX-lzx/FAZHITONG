-- ============================================================
-- 法智通 - 企业法律审核模块（2027 计划 · 企业法务全模块）
-- 执行: mysql> source d:/FAZHITONG/docs/phase6-legal-review.sql
-- ============================================================
USE fazhitong;

CREATE TABLE IF NOT EXISTS `legal_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `title` VARCHAR(256) DEFAULT NULL COMMENT '审核事项标题',
    `review_type` VARCHAR(32) DEFAULT 'CONTRACT' COMMENT 'CONTRACT/DOCUMENT/COMPLIANCE/OTHER',
    `content` TEXT COMMENT '审核内容摘要',
    `submitter` VARCHAR(64) DEFAULT NULL COMMENT '提交人/部门',
    `status` VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING/REVIEWING/APPROVED/REJECTED',
    `reviewer` VARCHAR(64) DEFAULT NULL COMMENT '审核人',
    `opinion` VARCHAR(512) DEFAULT NULL COMMENT '审核意见',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业法律审核台账';
