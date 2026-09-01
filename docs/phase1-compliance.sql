-- ============================================================
-- 法智通 Phase 1 · 合规体检问卷化 增量脚本
-- 使用方法: mysql -u root -p fazhitong < phase1-compliance.sql
-- ============================================================
USE fazhitong;

-- 合规体检题目表
CREATE TABLE IF NOT EXISTS `compliance_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `domain` VARCHAR(32) DEFAULT 'LABOR' COMMENT '领域: LABOR/CONTRACT/IP/DATA/TAX',
    `content` VARCHAR(500) NOT NULL COMMENT '题目内容',
    `weight` INT DEFAULT 5 COMMENT '权重 0-10',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` INT DEFAULT 1 COMMENT '1 启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_domain (`domain`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规体检题目';

-- 合规体检答题记录
CREATE TABLE IF NOT EXISTS `compliance_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `answer` VARCHAR(16) DEFAULT NULL COMMENT 'YES/NO/NA',
    `status` INT DEFAULT 0 COMMENT '0 草稿 1 已提交',
    `report_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ent_q (`enterprise_id`, `question_id`),
    INDEX idx_enterprise (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规体检答题记录';

-- 题目种子数据（五领域，各 4 题）
INSERT INTO `compliance_question` (`domain`, `content`, `weight`, `sort`) VALUES
('LABOR', '是否与全体劳动者签订了书面劳动合同', 8, 1),
('LABOR', '是否依法为员工缴纳社会保险', 8, 2),
('LABOR', '是否建立了完善的员工手册或规章制度并公示', 6, 3),
('LABOR', '是否依法支付加班费并执行工时制度', 6, 4),
('CONTRACT', '是否建立合同签订前的审批流程', 7, 1),
('CONTRACT', '是否对合同履约进度进行跟踪管理', 6, 2),
('CONTRACT', '是否对合同档案进行规范归档', 6, 3),
('CONTRACT', '是否对合同到期与续签进行预警管理', 7, 4),
('IP', '是否对核心商标进行了注册保护', 8, 1),
('IP', '是否对核心技术申请了专利或采取保密措施', 8, 2),
('IP', '是否建立了知识产权内部管理制度', 6, 3),
('IP', '是否对版权作品进行了登记或权属约定', 6, 4),
('DATA', '是否制定了个人信息保护制度', 8, 1),
('DATA', '是否公开了隐私政策并取得用户授权', 8, 2),
('DATA', '是否对敏感数据采取了加密或访问控制', 7, 3),
('DATA', '是否建立了数据安全事件应急预案', 7, 4),
('TAX', '是否按时完成纳税申报', 8, 1),
('TAX', '是否规范管理增值税发票', 7, 2),
('TAX', '是否建立了财务与税务内部核算制度', 6, 3),
('TAX', '是否存在税务筹划并确保合规', 6, 4);

-- 合同审查风险点表
CREATE TABLE IF NOT EXISTS `contract_review_risk` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL COMMENT '关联审查任务',
    `level` VARCHAR(16) DEFAULT 'LOW' COMMENT 'HIGH/MEDIUM/LOW',
    `clause` TEXT COMMENT '条款原文',
    `description` TEXT COMMENT '风险说明',
    `suggestion` TEXT COMMENT '修改建议',
    `legal_basis` TEXT COMMENT '法律依据',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同审查风险点';

-- 裁判文书表增加判决依据字段（智能提炼缓存）
ALTER TABLE `case_government`
    ADD COLUMN `judgment_basis` TEXT NULL COMMENT '判决依据（智能提炼缓存）' AFTER `focus_points`;
