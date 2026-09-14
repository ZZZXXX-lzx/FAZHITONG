-- ============================================================
-- phase6-role.sql  角色-权限体系初始化（角色管理升级）
-- 执行方式: mysql -uroot -p123456 fazhitong < docs/phase6-role.sql
-- 幂等: 已存在同名权限/关联时跳过
-- ============================================================

-- 1) 初始化权限点（对应平台服务模块）
INSERT INTO permission (permission_name, permission_code, api_identifier, status) VALUES
('案例检索', 'CASE_SEARCH', '/case/**', 1),
('法律法规', 'REGULATION', '/regulation/**', 1),
('知识库', 'KNOWLEDGE', '/knowledge/**', 1),
('法律咨询', 'CONSULTATION', '/consultation/**', 1),
('文书生成', 'DOCUMENT', '/document/**', 1),
('合同管理', 'CONTRACT', '/contract/**', 1),
('法律工具箱', 'TOOLBOX', '/toolbox/**', 1),
('法律援助', 'LEGAL_AID', '/legal-aid/**', 1),
('律师服务', 'LAWYER_SERVICE', '/lawyer-service/**', 1),
('法律体检', 'COMPLIANCE', '/compliance/**', 1),
('企业服务', 'ENTERPRISE', '/enterprise/**', 1),
('个人中心', 'PROFILE', '/profile/**', 1)
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 2) 角色默认权限分配（按平台各角色应见模块配置）
-- 辅助视图: 权限id
-- SUPER_ADMIN / PLATFORM_ADMIN: 全部
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.role_code IN ('SUPER_ADMIN', 'PLATFORM_ADMIN') AND p.status = 1;

-- LAWYER: 案例/法规/知识/咨询/文书/工具/律师服务
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.role_code = 'LAWYER'
  AND p.permission_code IN ('CASE_SEARCH','REGULATION','KNOWLEDGE','CONSULTATION','DOCUMENT','TOOLBOX','LAWYER_SERVICE');

-- USER: 案例/法规/知识/咨询/文书/合同/工具/体检/个人中心
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.role_code = 'USER'
  AND p.permission_code IN ('CASE_SEARCH','REGULATION','KNOWLEDGE','CONSULTATION','DOCUMENT','CONTRACT','TOOLBOX','COMPLIANCE','PROFILE');

-- ENTERPRISE_ADMIN / ENTERPRISE_USER: 企业相关 + 基础模块
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.role_code IN ('ENTERPRISE_ADMIN', 'ENTERPRISE_USER')
  AND p.permission_code IN ('CASE_SEARCH','REGULATION','KNOWLEDGE','DOCUMENT','CONTRACT','TOOLBOX','COMPLIANCE','ENTERPRISE','PROFILE');

-- GUEST: 公开浏览模块
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.role_code = 'GUEST'
  AND p.permission_code IN ('CASE_SEARCH','REGULATION','KNOWLEDGE');
