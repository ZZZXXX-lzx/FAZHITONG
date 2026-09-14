-- ============================================================
-- phase7-system-config.sql  系统配置（后台 SystemConfig 页）
-- 幂等：表不存在则创建；不覆盖已有配置
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_config (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  config_key    VARCHAR(64)  NOT NULL COMMENT '配置键',
  config_value  TEXT         NULL COMMENT '配置值',
  remark        VARCHAR(200) NULL COMMENT '说明',
  update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- 默认配置（幂等插入）
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
('platform_name', '法智通法律服务平台', '平台名称'),
('service_phone', '400-888-0000', '客服电话'),
('ai_model', 'deepseek', 'AI模型选择'),
('register_methods', '["phone"]', '注册方式(JSON数组: phone/email)');