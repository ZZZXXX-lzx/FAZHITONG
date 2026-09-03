-- ============================================================
-- 法智通 - 企业投融资管理模块（2027 计划 · 企业法务全模块）
-- 执行: mysql> source d:/FAZHITONG/docs/phase5-investment.sql
-- ============================================================
USE fazhitong;

CREATE TABLE IF NOT EXISTS `investment_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `type` VARCHAR(32) NOT NULL DEFAULT 'FINANCING' COMMENT 'FINANCING 融资 / INVESTMENT 对外投资',
    `round` VARCHAR(32) DEFAULT NULL COMMENT '天使轮/Pre-A/A轮/B轮/C轮/D轮/战略投资/其他',
    `investor` VARCHAR(128) DEFAULT NULL COMMENT '投资方 / 被投企业',
    `amount` DECIMAL(18,2) DEFAULT NULL COMMENT '金额（万元）',
    `valuation` DECIMAL(18,2) DEFAULT NULL COMMENT '投后估值（万元）',
    `equity_ratio` DECIMAL(10,4) DEFAULT NULL COMMENT '股权比例（%）',
    `invest_date` DATETIME DEFAULT NULL COMMENT '时间',
    `status` VARCHAR(32) DEFAULT 'PLANNED' COMMENT 'PLANNED/IN_PROGRESS/COMPLETED/TERMINATED',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业投融资台账';
