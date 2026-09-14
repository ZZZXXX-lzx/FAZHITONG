-- ============================================================
-- 阶段5：文书生成模块升级
-- 为 document_record 增加 doc_name 列，用于展示"我的文书"记录标题
-- 执行前确认服务未写入该表或加列后可容忍短暂缺列（先执行后重启更稳妥）
-- ============================================================
ALTER TABLE `document_record`
    ADD COLUMN `doc_name` VARCHAR(255) DEFAULT NULL COMMENT '文书标题（模板名或 AI 起草标题）' AFTER `template_id`;