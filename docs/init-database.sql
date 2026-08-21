-- ============================================================
-- 法保通法律服务平台 - 数据库一键初始化脚本
-- 执行顺序: 1.建库建表 → 2.扩展功能表 → 3.初始数据
-- 使用方法: mysql -u root -p < init-database.sql
-- ============================================================

-- ===== Part 1: 基础表结构 =====
CREATE DATABASE IF NOT EXISTS fabaotong DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fabaotong;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `account` VARCHAR(64) NOT NULL UNIQUE COMMENT '账号',
    `password` VARCHAR(256) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像',
    `user_type` VARCHAR(32) DEFAULT 'USER' COMMENT '用户类型: USER/LAWYER/ENTERPRISE/ADMIN',
    `status` INT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_account (`account`),
    INDEX idx_user_type (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(64) NOT NULL,
    `role_code` VARCHAR(64) NOT NULL UNIQUE,
    `description` VARCHAR(256) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_name` VARCHAR(64) NOT NULL,
    `permission_code` VARCHAR(64) NOT NULL UNIQUE,
    `menu_id` BIGINT DEFAULT NULL,
    `api_identifier` VARCHAR(128) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 文书模板表
CREATE TABLE IF NOT EXISTS `document_template` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(64) NOT NULL,
    `name` VARCHAR(128) NOT NULL,
    `content` TEXT,
    `category` VARCHAR(64) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (`type`),
    INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文书模板';

-- 文书生成记录
CREATE TABLE IF NOT EXISTS `document_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `template_id` BIGINT DEFAULT NULL,
    `data` TEXT,
    `file_url` VARCHAR(512) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文书生成记录';

-- 文书分类
CREATE TABLE IF NOT EXISTS `document_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL,
    `code` VARCHAR(64) NOT NULL UNIQUE,
    `parent_id` BIGINT DEFAULT 0,
    `sort` INT DEFAULT 0,
    `status` INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文书分类';

-- 咨询表
CREATE TABLE IF NOT EXISTS `consultation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `lawyer_id` BIGINT DEFAULT NULL,
    `title` VARCHAR(256) DEFAULT NULL,
    `question` TEXT,
    `answer` TEXT,
    `consultation_type` VARCHAR(32) DEFAULT 'AI',
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `answer_time` DATETIME DEFAULT NULL,
    INDEX idx_user_id (`user_id`),
    INDEX idx_lawyer_id (`lawyer_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询表';

-- 合同审查记录
CREATE TABLE IF NOT EXISTS `contract_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT DEFAULT NULL,
    `enterprise_id` BIGINT DEFAULT NULL,
    `title` VARCHAR(256) DEFAULT NULL,
    `file_url` VARCHAR(512) DEFAULT NULL,
    `risk_report` TEXT,
    `risk_level` VARCHAR(32) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `review_time` DATETIME DEFAULT NULL,
    INDEX idx_user_id (`user_id`),
    INDEX idx_enterprise_id (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同审查记录';

-- 企业合同表
CREATE TABLE IF NOT EXISTS `enterprise_contract` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `title` VARCHAR(256) DEFAULT NULL,
    `contract_no` VARCHAR(64) DEFAULT NULL,
    `file_url` VARCHAR(512) DEFAULT NULL,
    `party_a` VARCHAR(128) DEFAULT NULL,
    `party_b` VARCHAR(128) DEFAULT NULL,
    `amount` VARCHAR(64) DEFAULT NULL,
    `status` VARCHAR(32) DEFAULT 'DRAFT',
    `sign_date` DATE DEFAULT NULL,
    `expire_date` DATE DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise_id (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业合同';

-- 案例/裁判文书表
CREATE TABLE IF NOT EXISTS `case_government` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cause_name` VARCHAR(128) DEFAULT NULL,
    `court_name` VARCHAR(128) DEFAULT NULL,
    `case_year` VARCHAR(16) DEFAULT NULL,
    `judgment_result` TEXT,
    `keywords` VARCHAR(256) DEFAULT NULL,
    `abstract` TEXT,
    `focus_points` TEXT,
    `full_text` LONGTEXT,
    `source` VARCHAR(64) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cause_name (`cause_name`),
    INDEX idx_keywords (`keywords`),
    FULLTEXT INDEX ft_full_text (`full_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='裁判文书';

-- 律师信息表
CREATE TABLE IF NOT EXISTS `lawyer_info` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `practice_cert_no` VARCHAR(64) DEFAULT NULL,
    `law_firm` VARCHAR(128) DEFAULT NULL,
    `specialty` VARCHAR(256) DEFAULT NULL,
    `years_exp` INT DEFAULT 0,
    `description` TEXT,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律师信息';

-- 企业信息表
CREATE TABLE IF NOT EXISTS `enterprise_info` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `enterprise_name` VARCHAR(128) NOT NULL,
    `credit_code` VARCHAR(64) DEFAULT NULL,
    `enterprise_scale` VARCHAR(32) DEFAULT NULL,
    `contact_name` VARCHAR(64) DEFAULT NULL,
    `contact_phone` VARCHAR(20) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `order_no` VARCHAR(64) NOT NULL UNIQUE,
    `order_type` VARCHAR(32) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `pay_time` DATETIME DEFAULT NULL,
    INDEX idx_user_id (`user_id`),
    INDEX idx_order_no (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 支付记录
CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `payment_method` VARCHAR(32) DEFAULT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `transaction_no` VARCHAR(128) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- 会员表
CREATE TABLE IF NOT EXISTS `member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `member_type` VARCHAR(32) DEFAULT 'STANDARD',
    `expire_date` DATETIME DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';

-- 合规报告
CREATE TABLE IF NOT EXISTS `compliance_report` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `enterprise_id` BIGINT NOT NULL,
    `report_type` VARCHAR(64) DEFAULT NULL,
    `score` INT DEFAULT 0,
    `report_data` TEXT,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise_id (`enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规报告';

-- ===== Part 2: 扩展功能表 =====
-- 法律知识库
CREATE TABLE IF NOT EXISTS `knowledge_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL,
    `code` VARCHAR(64) NOT NULL UNIQUE,
    `parent_id` BIGINT DEFAULT 0,
    `sort` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类';

CREATE TABLE IF NOT EXISTS `knowledge_article` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category_id` BIGINT DEFAULT NULL,
    `title` VARCHAR(256) NOT NULL,
    `summary` VARCHAR(512) DEFAULT NULL,
    `content` LONGTEXT,
    `tags` VARCHAR(256) DEFAULT NULL,
    `source` VARCHAR(128) DEFAULT NULL,
    `author` VARCHAR(64) DEFAULT NULL,
    `view_count` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `is_top` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (`category_id`),
    INDEX idx_status (`status`),
    FULLTEXT INDEX ft_content (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文章';

-- 消息通知
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(256) NOT NULL,
    `content` TEXT,
    `type` VARCHAR(32) DEFAULT 'SYSTEM',
    `ref_id` BIGINT DEFAULT NULL,
    `is_read` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (`user_id`),
    INDEX idx_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- 评价评分
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `target_type` VARCHAR(32) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `rating` INT DEFAULT 5,
    `content` TEXT,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (`target_type`, `target_id`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价评分';

-- 收藏
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `target_type` VARCHAR(32) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fav (`user_id`, `target_type`, `target_id`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';

-- 意见反馈
CREATE TABLE IF NOT EXISTS `feedback` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT DEFAULT NULL,
    `type` VARCHAR(32) DEFAULT 'SUGGESTION',
    `title` VARCHAR(256) NOT NULL,
    `content` TEXT NOT NULL,
    `contact` VARCHAR(128) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `reply` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `reply_time` DATETIME DEFAULT NULL,
    INDEX idx_status (`status`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈';

-- 法律援助申请
CREATE TABLE IF NOT EXISTS `legal_aid` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `applicant_name` VARCHAR(64) NOT NULL,
    `id_card` VARCHAR(18) DEFAULT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `address` VARCHAR(256) DEFAULT NULL,
    `case_type` VARCHAR(64) DEFAULT NULL,
    `case_desc` TEXT NOT NULL,
    `evidence_urls` VARCHAR(1024) DEFAULT NULL,
    `income_status` VARCHAR(128) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `assigned_lawyer_id` BIGINT DEFAULT NULL,
    `review_remark` VARCHAR(512) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `review_time` DATETIME DEFAULT NULL,
    INDEX idx_user (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='法律援助申请';

-- 律师服务委托
CREATE TABLE IF NOT EXISTS `lawyer_service` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `lawyer_id` BIGINT NOT NULL,
    `service_type` VARCHAR(64) NOT NULL,
    `title` VARCHAR(256) NOT NULL,
    `description` TEXT NOT NULL,
    `budget` DECIMAL(10,2) DEFAULT NULL,
    `status` INT DEFAULT 0,
    `lawyer_remark` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `accept_time` DATETIME DEFAULT NULL,
    `finish_time` DATETIME DEFAULT NULL,
    INDEX idx_user (`user_id`),
    INDEX idx_lawyer (`lawyer_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律师服务委托';

-- 律师服务价格
CREATE TABLE IF NOT EXISTS `lawyer_service_price` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lawyer_id` BIGINT NOT NULL,
    `service_type` VARCHAR(64) NOT NULL,
    `service_name` VARCHAR(128) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `unit` VARCHAR(32) DEFAULT '次',
    `description` VARCHAR(512) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lawyer (`lawyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='律师服务价格';

-- ===== Part 3: 初始数据 =====
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统最高权限'),
('平台管理员', 'PLATFORM_ADMIN', '平台运营管理'),
('企业管理员', 'ENTERPRISE_ADMIN', '企业管理'),
('企业员工', 'ENTERPRISE_USER', '企业普通员工'),
('认证律师', 'LAWYER', '认证执业律师'),
('普通用户', 'USER', '个人用户'),
('访客', 'GUEST', '未登录访客');

INSERT INTO `document_category` (`name`, `code`, `parent_id`, `sort`) VALUES
('民事起诉状', 'CIVIL_COMPLAINT', 0, 1),
('刑事起诉状', 'CRIMINAL_COMPLAINT', 0, 2),
('劳动仲裁', 'LABOR_ARBITRATION', 0, 3),
('上诉状', 'APPEAL', 0, 4),
('答辩状', 'DEFENSE', 0, 5),
('协议书', 'AGREEMENT', 0, 6),
('强制执行', 'ENFORCEMENT', 0, 7);

INSERT INTO `knowledge_category` (`name`, `code`, `parent_id`, `sort`) VALUES
('法律法规', 'LAWS_REGULATIONS', 0, 1),
('法律常识', 'LEGAL_COMMON', 0, 2),
('合同纠纷', 'CONTRACT_DISPUTE', 0, 3),
('婚姻家庭', 'MARRIAGE_FAMILY', 0, 4),
('劳动争议', 'LABOR_DISPUTE', 0, 5),
('交通事故', 'TRAFFIC_ACCIDENT', 0, 6),
('知识产权', 'INTELLECTUAL_PROPERTY', 0, 7),
('刑事辩护', 'CRIMINAL_DEFENSE', 0, 8),
('房产纠纷', 'PROPERTY_DISPUTE', 0, 9),
('消费维权', 'CONSUMER_RIGHTS', 0, 10);

INSERT INTO `knowledge_article` (`category_id`, `title`, `summary`, `content`, `tags`, `author`) VALUES
(2, '什么是诉讼时效？', '了解诉讼时效的基本概念和法律规定', '诉讼时效是指权利人在法定期间内不行使权利，就丧失了请求人民法院保护其民事权利的法律制度。我国《民法典》规定，一般诉讼时效为三年，自权利人知道或者应当知道权利受到损害以及义务人之日起计算。', '诉讼时效,民法典,民事权利', '法保通'),
(2, '如何写一份有效的借条？', '借条的法律要素和注意事项', '借条是表明债权债务关系的书面凭证。一份有效的借条应当包含：借款人和出借人的身份信息、借款金额（大小写）、借款用途、借款期限、利率约定（不得超过法律规定的上限）、借款日期、借款人签字捺印。', '借条,借贷,债权债务', '法保通'),
(3, '合同违约后的法律救济', '合同违约的赔偿方式和救济途径', '当一方违约时，守约方可以要求继续履行、采取补救措施、赔偿损失等。赔偿额应当相当于因违约所造成的损失，包括合同履行后可以获得的利益。', '合同违约,赔偿,救济', '法保通'),
(4, '离婚财产如何分割？', '离婚时夫妻共同财产的分割原则', '离婚时，夫妻共同财产由双方协议处理；协议不成时，由人民法院根据财产的具体情况，按照照顾子女、女方和无过错方权益的原则判决。', '离婚,财产分割,婚姻法', '法保通'),
(5, '用人单位拖欠工资怎么办？', '劳动者追讨工资的法律途径', '用人单位拖欠工资的，劳动者可以：1.向劳动监察部门投诉；2.申请劳动仲裁；3.对仲裁结果不服的可以向法院起诉；4.申请支付令。', '拖欠工资,劳动仲裁,维权', '法保通'),
(6, '交通事故赔偿标准', '交通事故人身损害赔偿的计算标准', '交通事故赔偿项目包括：医疗费、误工费、护理费、交通费、住宿费、住院伙食补助费、营养费、残疾赔偿金、残疾辅助器具费、被扶养人生活费、精神损害抚慰金等。', '交通事故,赔偿,人身损害', '法保通'),
(7, '商标侵权的认定标准', '商标侵权的构成要件和救济方式', '商标侵权行为包括：未经商标注册人许可在同一种或类似商品上使用相同或近似商标、销售侵犯注册商标专用权的商品等。权利人可要求停止侵权、赔偿损失。', '商标,知识产权,侵权', '法保通'),
(8, '刑事拘留的最长期限', '刑事诉讼中拘留期限的法律规定', '公安机关对被拘留的人，认为需要逮捕的，应当在拘留后三日内提请检察院审查批准。特殊情况下可以延长一至四日。对流窜作案、多次作案、结伙作案的重大嫌疑分子可以延长至三十日。', '刑事拘留,刑诉法,期限', '法保通');

-- 管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `user` (`account`, `password`, `nickname`, `user_type`, `status`) VALUES
('admin', '$2a$10$N.ZOn9G6/YLFixbkPSJjJe9DT3TnV1B5nE6jKqjVqFSq6Zf7B7B7', '系统管理员', 'ADMIN', 1);
