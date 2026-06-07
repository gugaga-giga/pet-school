-- ============================================================================
-- 基于Transformer和RAG的智能售前客服机器人 - 数据库初始化脚本
-- ============================================================================
-- 数据库类型: MySQL 8.0+
-- 字符集:   utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- 设计规范: 第三范式 (3NF)
-- 数据规模: 支持百万级数据量
-- 特性支持: 软删除、审计字段、JSON扩展字段、外键约束
-- 创建日期: 2026-06-04
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 数据库创建（如需新建数据库请取消注释）
-- ----------------------------------------------------------------------------
-- CREATE DATABASE IF NOT EXISTS `smart_presale_bot`
--   DEFAULT CHARACTER SET utf8mb4
--   DEFAULT COLLATE utf8mb4_unicode_ci;
-- USE `smart_presale_bot`;

-- ============================================================================
-- 1. users - 用户表
-- ============================================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(64)      NOT NULL COMMENT '用户名',
  `email`         VARCHAR(128)     NOT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255)     NOT NULL COMMENT '密码哈希',
  `nickname`      VARCHAR(64)      DEFAULT NULL COMMENT '昵称',
  `avatar`        VARCHAR(512)     DEFAULT NULL COMMENT '头像URL',
  `phone`         VARCHAR(20)      DEFAULT NULL COMMENT '手机号',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted`    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- 2. roles - 角色表
-- ============================================================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name`        VARCHAR(64)      NOT NULL COMMENT '角色名称',
  `code`        VARCHAR(64)      NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255)     DEFAULT NULL COMMENT '角色描述',
  `is_deleted`  TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================================
-- 3. permissions - 权限表
-- ============================================================================
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name`        VARCHAR(64)      NOT NULL COMMENT '权限名称',
  `code`        VARCHAR(128)     NOT NULL COMMENT '权限编码',
  `resource`    VARCHAR(64)      NOT NULL COMMENT '资源标识',
  `action`      VARCHAR(32)      NOT NULL COMMENT '操作类型: create/read/update/delete',
  `description` VARCHAR(255)     DEFAULT NULL COMMENT '权限描述',
  `is_deleted`  TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`),
  KEY `idx_resource` (`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================================
-- 4. user_roles - 用户角色关联表
-- ============================================================================
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id`    BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================================
-- 5. role_permissions - 角色权限关联表
-- ============================================================================
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id`       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permissions_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permissions_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================================
-- 6. pets - 宠物表
-- ============================================================================
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
  `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `user_id`        BIGINT UNSIGNED  NOT NULL COMMENT '所属用户ID',
  `name`           VARCHAR(64)      NOT NULL COMMENT '宠物名称',
  `type`           VARCHAR(32)      NOT NULL COMMENT '宠物类型: cat/dog/rabbit/bird/hamster/other',
  `breed`          VARCHAR(64)      DEFAULT NULL COMMENT '品种',
  `birth_date`     DATE             DEFAULT NULL COMMENT '出生日期',
  `age`            INT UNSIGNED     DEFAULT NULL COMMENT '年龄(月)',
  `weight`         DECIMAL(5,2)     DEFAULT NULL COMMENT '体重(kg)',
  `gender`         TINYINT UNSIGNED DEFAULT NULL COMMENT '性别: 0-未知 1-公 2-母',
  `is_neutered`    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否绝育: 0-否 1-是',
  `health_status`  VARCHAR(32)      NOT NULL DEFAULT 'healthy' COMMENT '健康状态: healthy/subhealthy/ill/recovering',
  `vaccine_status` VARCHAR(32)      NOT NULL DEFAULT 'unknown' COMMENT '疫苗状态: unknown/incomplete/complete/overdue',
  `is_deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_breed` (`breed`),
  CONSTRAINT `fk_pets_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物表';

-- ============================================================================
-- 7. knowledge_bases - 知识库表
-- ============================================================================
DROP TABLE IF EXISTS `knowledge_bases`;
CREATE TABLE `knowledge_bases` (
  `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `name`           VARCHAR(128)     NOT NULL COMMENT '知识库名称',
  `description`    TEXT             DEFAULT NULL COMMENT '知识库描述',
  `icon`           VARCHAR(512)     DEFAULT NULL COMMENT '图标URL',
  `document_count` INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '文档数量',
  `chunk_count`    INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '文档块数量',
  `owner_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所有者用户ID',
  `is_public`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否公开: 0-否 1-是',
  `status`         TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_knowledge_bases_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ============================================================================
-- 8. documents - 文档表
-- ============================================================================
DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `knowledge_base_id` BIGINT UNSIGNED  NOT NULL COMMENT '所属知识库ID',
  `title`             VARCHAR(255)     NOT NULL COMMENT '文档标题',
  `file_name`         VARCHAR(255)     NOT NULL COMMENT '文件名',
  `file_path`         VARCHAR(512)     NOT NULL COMMENT '文件存储路径',
  `file_type`         VARCHAR(32)      NOT NULL COMMENT '文件类型: pdf/docx/txt/md/csv/xlsx',
  `file_size`         BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `chunk_count`       INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '文档块数量',
  `status`            VARCHAR(32)      NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/completed/failed',
  `is_deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_base_id` (`knowledge_base_id`),
  KEY `idx_status` (`status`),
  KEY `idx_file_type` (`file_type`),
  CONSTRAINT `fk_documents_knowledge_base_id` FOREIGN KEY (`knowledge_base_id`) REFERENCES `knowledge_bases` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- ============================================================================
-- 9. document_chunks - 文档块表
-- ============================================================================
DROP TABLE IF EXISTS `document_chunks`;
CREATE TABLE `document_chunks` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '文档块ID',
  `document_id`       BIGINT UNSIGNED  NOT NULL COMMENT '所属文档ID',
  `knowledge_base_id` BIGINT UNSIGNED  NOT NULL COMMENT '所属知识库ID',
  `chunk_index`       INT UNSIGNED     NOT NULL COMMENT '块序号',
  `content`           TEXT             NOT NULL COMMENT '块内容',
  `token_count`       INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT 'Token数量',
  `faiss_id`          BIGINT           DEFAULT NULL COMMENT 'FAISS向量索引ID',
  `metadata`          JSON             DEFAULT NULL COMMENT '元数据(JSON)',
  `is_deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_knowledge_base_id` (`knowledge_base_id`),
  KEY `idx_faiss_id` (`faiss_id`),
  CONSTRAINT `fk_document_chunks_document_id` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_document_chunks_knowledge_base_id` FOREIGN KEY (`knowledge_base_id`) REFERENCES `knowledge_bases` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档块表';

-- ============================================================================
-- 10. chat_sessions - 聊天会话表
-- ============================================================================
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id`           BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
  `title`             VARCHAR(255)     NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `knowledge_base_id` BIGINT UNSIGNED  DEFAULT NULL COMMENT '关联知识库ID',
  `is_deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_knowledge_base_id` (`knowledge_base_id`),
  CONSTRAINT `fk_chat_sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_sessions_knowledge_base_id` FOREIGN KEY (`knowledge_base_id`) REFERENCES `knowledge_bases` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ============================================================================
-- 11. chat_messages - 聊天消息表
-- ============================================================================
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id`          BIGINT UNSIGNED  NOT NULL COMMENT '会话ID',
  `role`                VARCHAR(16)      NOT NULL COMMENT '角色: user/assistant/system',
  `content`             TEXT             NOT NULL COMMENT '消息内容',
  `sources`             JSON             DEFAULT NULL COMMENT '引用来源(JSON)',
  `intent_type`         VARCHAR(64)      DEFAULT NULL COMMENT '意图类型',
  `recommended_products` JSON            DEFAULT NULL COMMENT '推荐产品列表(JSON)',
  `token_count`         INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT 'Token消耗数量',
  `is_deleted`          TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_role` (`role`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_chat_messages_session_id` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ============================================================================
-- 12. qa_logs - 问答日志表
-- ============================================================================
DROP TABLE IF EXISTS `qa_logs`;
CREATE TABLE `qa_logs` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
  `session_id`    BIGINT UNSIGNED  NOT NULL COMMENT '会话ID',
  `question`      TEXT             NOT NULL COMMENT '用户问题',
  `answer`        TEXT             NOT NULL COMMENT '系统回答',
  `intent_type`   VARCHAR(64)      DEFAULT NULL COMMENT '意图类型',
  `sources`       JSON             DEFAULT NULL COMMENT '引用来源(JSON)',
  `retrieval_time` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '检索耗时(ms)',
  `llm_time`      INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT 'LLM推理耗时(ms)',
  `total_time`    INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '总耗时(ms)',
  `is_deleted`    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_intent_type` (`intent_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_total_time` (`total_time`),
  CONSTRAINT `fk_qa_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_qa_logs_session_id` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答日志表';

-- ============================================================================
-- 13. products - 产品表
-- ============================================================================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `name`          VARCHAR(128)     NOT NULL COMMENT '产品名称',
  `category`      VARCHAR(64)      NOT NULL COMMENT '产品分类: food/toy/health/cleaning/accessory/other',
  `description`   TEXT             DEFAULT NULL COMMENT '产品描述',
  `price`         DECIMAL(10,2)    NOT NULL DEFAULT 0.00 COMMENT '价格(元)',
  `features`      JSON             DEFAULT NULL COMMENT '产品特点(JSON)',
  `specifications` JSON            DEFAULT NULL COMMENT '产品规格(JSON)',
  `image_url`     VARCHAR(512)     DEFAULT NULL COMMENT '产品图片URL',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `is_deleted`    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- ============================================================================
-- 14. recommend_rules - 推荐规则表
-- ============================================================================
DROP TABLE IF EXISTS `recommend_rules`;
CREATE TABLE `recommend_rules` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `name`        VARCHAR(128)     NOT NULL COMMENT '规则名称',
  `intent_type` VARCHAR(64)      NOT NULL COMMENT '意图类型',
  `keywords`    JSON             DEFAULT NULL COMMENT '触发关键词(JSON数组)',
  `product_ids` JSON             DEFAULT NULL COMMENT '推荐产品ID列表(JSON数组)',
  `priority`    INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '优先级(数值越大优先级越高)',
  `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted`  TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_intent_type` (`intent_type`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐规则表';

-- ============================================================================
-- 15. system_configs - 系统配置表
-- ============================================================================
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key`   VARCHAR(128)     NOT NULL COMMENT '配置键',
  `config_value` TEXT             NOT NULL COMMENT '配置值',
  `description`  VARCHAR(255)     DEFAULT NULL COMMENT '配置描述',
  `value_type`   VARCHAR(32)      NOT NULL DEFAULT 'string' COMMENT '值类型: string/number/boolean/json',
  `is_deleted`   TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 16. operation_logs - 操作日志表
-- ============================================================================
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`    BIGINT UNSIGNED  DEFAULT NULL COMMENT '操作用户ID',
  `action`     VARCHAR(64)      NOT NULL COMMENT '操作类型',
  `resource`   VARCHAR(64)      NOT NULL COMMENT '操作资源',
  `detail`     JSON             DEFAULT NULL COMMENT '操作详情(JSON)',
  `ip_address` VARCHAR(45)      DEFAULT NULL COMMENT 'IP地址(兼容IPv6)',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
  `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_operation_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 初始数据
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 初始管理员用户 (密码hash为占位符，部署时需替换)
-- ----------------------------------------------------------------------------
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `nickname`, `status`) VALUES
(1, 'admin', 'admin@example.com', '$2b$12$PLACEHOLDER_REPLACE_WITH_REAL_HASH', '系统管理员', 1);

-- ----------------------------------------------------------------------------
-- 初始角色
-- ----------------------------------------------------------------------------
INSERT INTO `roles` (`id`, `name`, `code`, `description`) VALUES
(1, '超级管理员', 'super_admin', '拥有系统所有权限的超级管理员'),
(2, '管理员',     'admin',       '系统管理员，可管理用户和知识库'),
(3, '普通用户',   'user',        '普通用户，可使用对话和宠物管理功能');

-- ----------------------------------------------------------------------------
-- 初始权限
-- ----------------------------------------------------------------------------
INSERT INTO `permissions` (`id`, `name`, `code`, `resource`, `action`, `description`) VALUES
-- 用户管理
(1,  '查看用户',   'user:read',       'user',       'read',   '查看用户信息'),
(2,  '创建用户',   'user:create',     'user',       'create', '创建新用户'),
(3,  '编辑用户',   'user:update',     'user',       'update', '编辑用户信息'),
(4,  '删除用户',   'user:delete',     'user',       'delete', '删除用户'),
-- 角色管理
(5,  '查看角色',   'role:read',       'role',       'read',   '查看角色信息'),
(6,  '创建角色',   'role:create',     'role',       'create', '创建新角色'),
(7,  '编辑角色',   'role:update',     'role',       'update', '编辑角色信息'),
(8,  '删除角色',   'role:delete',     'role',       'delete', '删除角色'),
-- 权限管理
(9,  '查看权限',   'permission:read', 'permission', 'read',   '查看权限信息'),
(10, '创建权限',   'permission:create','permission', 'create', '创建新权限'),
(11, '编辑权限',   'permission:update','permission', 'update', '编辑权限信息'),
(12, '删除权限',   'permission:delete','permission', 'delete', '删除权限'),
-- 知识库管理
(13, '查看知识库', 'kb:read',         'knowledge_base', 'read',   '查看知识库'),
(14, '创建知识库', 'kb:create',       'knowledge_base', 'create', '创建知识库'),
(15, '编辑知识库', 'kb:update',       'knowledge_base', 'update', '编辑知识库'),
(16, '删除知识库', 'kb:delete',       'knowledge_base', 'delete', '删除知识库'),
-- 文档管理
(17, '查看文档',   'doc:read',        'document',       'read',   '查看文档'),
(18, '上传文档',   'doc:create',      'document',       'create', '上传文档'),
(19, '编辑文档',   'doc:update',      'document',       'update', '编辑文档'),
(20, '删除文档',   'doc:delete',      'document',       'delete', '删除文档'),
-- 宠物管理
(21, '查看宠物',   'pet:read',        'pet',            'read',   '查看宠物信息'),
(22, '创建宠物',   'pet:create',      'pet',            'create', '添加宠物'),
(23, '编辑宠物',   'pet:update',      'pet',            'update', '编辑宠物信息'),
(24, '删除宠物',   'pet:delete',      'pet',            'delete', '删除宠物'),
-- 产品管理
(25, '查看产品',   'product:read',    'product',        'read',   '查看产品'),
(26, '创建产品',   'product:create',  'product',        'create', '创建产品'),
(27, '编辑产品',   'product:update',  'product',        'update', '编辑产品'),
(28, '删除产品',   'product:delete',  'product',        'delete', '删除产品'),
-- 推荐规则管理
(29, '查看推荐规则', 'rule:read',     'recommend_rule', 'read',   '查看推荐规则'),
(30, '创建推荐规则', 'rule:create',   'recommend_rule', 'create', '创建推荐规则'),
(31, '编辑推荐规则', 'rule:update',   'recommend_rule', 'update', '编辑推荐规则'),
(32, '删除推荐规则', 'rule:delete',   'recommend_rule', 'delete', '删除推荐规则'),
-- 对话管理
(33, '查看对话',   'chat:read',       'chat',           'read',   '查看对话记录'),
(34, '发起对话',   'chat:create',     'chat',           'create', '发起新对话'),
(35, '删除对话',   'chat:delete',     'chat',           'delete', '删除对话'),
-- 系统配置
(36, '查看配置',   'config:read',     'system_config',  'read',   '查看系统配置'),
(37, '修改配置',   'config:update',   'system_config',  'update', '修改系统配置'),
-- 操作日志
(38, '查看日志',   'log:read',        'operation_log',  'read',   '查看操作日志');

-- ----------------------------------------------------------------------------
-- 初始用户角色关联 (admin用户 -> 超级管理员)
-- ----------------------------------------------------------------------------
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1);

-- ----------------------------------------------------------------------------
-- 初始角色权限关联 (超级管理员 -> 所有权限)
-- ----------------------------------------------------------------------------
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),  (1, 7),  (1, 8),
(1, 9),  (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24),
(1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32),
(1, 33), (1, 34), (1, 35), (1, 36), (1, 37), (1, 38);

-- 管理员权限 (用户管理 + 知识库 + 文档 + 产品 + 推荐规则 + 对话 + 日志查看)
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(2, 1),  (2, 5),  (2, 9),  (2, 13), (2, 14), (2, 15), (2, 16),
(2, 17), (2, 18), (2, 19), (2, 20), (2, 25), (2, 26), (2, 27), (2, 28),
(2, 29), (2, 30), (2, 31), (2, 32), (2, 33), (2, 34), (2, 38);

-- 普通用户权限 (宠物 + 对话 + 查看产品)
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(3, 21), (3, 22), (3, 23), (3, 24), (3, 25), (3, 33), (3, 34), (3, 35);

-- ----------------------------------------------------------------------------
-- 初始系统配置
-- ----------------------------------------------------------------------------
INSERT INTO `system_configs` (`config_key`, `config_value`, `description`, `value_type`) VALUES
('llm.model_name',              'gpt-4',                                    'LLM模型名称',                       'string'),
('llm.temperature',             '0.7',                                      'LLM温度参数',                       'number'),
('llm.max_tokens',              '2048',                                     'LLM最大生成Token数',                'number'),
('llm.top_p',                   '0.9',                                      'LLM Top-P参数',                     'number'),
('rag.chunk_size',              '512',                                      '文档分块大小(Token)',               'number'),
('rag.chunk_overlap',           '64',                                       '文档分块重叠(Token)',               'number'),
('rag.top_k',                   '5',                                        'RAG检索返回文档块数量',             'number'),
('rag.similarity_threshold',    '0.7',                                      'RAG相似度阈值',                     'number'),
('rag.embedding_model',         'text-embedding-ada-002',                   'Embedding模型名称',                 'string'),
('chat.welcome_message',        '您好！我是智能宠物顾问，有什么可以帮您的吗？', '欢迎语',                           'string'),
('chat.max_history',            '20',                                       '对话历史最大轮数',                  'number'),
('chat.session_timeout',        '1800',                                     '会话超时时间(秒)',                  'number'),
('system.site_name',            '智能售前客服机器人',                         '站点名称',                          'string'),
('system.max_upload_size',      '52428800',                                 '最大上传文件大小(字节,默认50MB)',    'number'),
('system.allowed_file_types',   '["pdf","docx","txt","md","csv","xlsx"]',   '允许上传的文件类型',                'json'),
('system.log_retention_days',   '90',                                       '日志保留天数',                      'number');
