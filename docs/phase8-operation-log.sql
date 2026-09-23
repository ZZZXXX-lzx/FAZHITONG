-- ============================================================
-- phase8-operation-log.sql  操作日志（admin 后台「操作日志」页）
-- 幂等：表不存在则创建
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       NULL COMMENT '操作用户ID',
  username      VARCHAR(64)  NULL COMMENT '操作人',
  action        VARCHAR(255) NULL COMMENT '操作内容',
  ip            VARCHAR(64)  NULL COMMENT 'IP地址',
  result        VARCHAR(20)  NULL COMMENT '结果(成功/失败)',
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';
