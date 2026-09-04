-- ============================================================
-- 法智通 - 律所团队管理模块（2029 计划 · 律所数字化管理）
-- 执行: mysql> source d:/FAZHITONG/docs/phase7-team.sql
-- ============================================================
USE fazhitong;

CREATE TABLE IF NOT EXISTS `team_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lawyer_id` BIGINT NOT NULL COMMENT '所属律师/团队负责人用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '成员姓名',
    `role` VARCHAR(32) DEFAULT 'LAWYER' COMMENT 'PARTNER/LAWYER/ASSISTANT/INTERN/ADMIN',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `status` VARCHAR(32) DEFAULT 'ACTIVE' COMMENT 'ACTIVE 在职 / INACTIVE 离职',
    `join_date` DATE DEFAULT NULL COMMENT '入职日期',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lawyer (`lawyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律所团队成员';
