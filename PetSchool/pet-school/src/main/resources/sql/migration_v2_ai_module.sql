-- ============================================================
-- Migration V2: AI模块集成
-- 版本: v2
-- 日期: 2026-06-06
-- 描述: 将AI售前机器人改造为PetSchool内部模块，新增聊天会话、
--       聊天消息、用户记忆档案三张表。脚本幂等，不影响现有业务表。
-- ============================================================

-- -----------------------------------------------------------
-- 1. 新增表结构
-- -----------------------------------------------------------

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '会话标题',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `sources` JSON DEFAULT NULL COMMENT 'RAG检索来源(JSON)',
    `intent_type` VARCHAR(30) DEFAULT NULL COMMENT '意图类型: FAQ/Course/PetKnowledge/Order/PetProfile/UserInfo/Greeting/Other',
    `token_count` INT DEFAULT 0 COMMENT 'Token数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

-- 用户记忆档案表
CREATE TABLE IF NOT EXISTS `ai_memory_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记忆ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `key` VARCHAR(50) NOT NULL COMMENT '记忆键: pet_preference/training_need/health_concern/schedule_preference/budget_range/other',
    `value` VARCHAR(500) NOT NULL COMMENT '记忆值',
    `source` VARCHAR(20) NOT NULL DEFAULT 'chat' COMMENT '来源: chat/manual/system',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_key` (`user_id`, `key`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户长期记忆档案表';

-- -----------------------------------------------------------
-- 2. 回滚脚本（取消下方注释即可执行回滚）
-- -----------------------------------------------------------

-- DROP TABLE IF EXISTS `ai_chat_message`;
-- DROP TABLE IF EXISTS `ai_chat_session`;
-- DROP TABLE IF EXISTS `ai_memory_profile`;

-- -----------------------------------------------------------
-- 3. 数据验证查询
-- -----------------------------------------------------------

-- 检查AI相关表是否创建成功
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('ai_chat_session', 'ai_chat_message', 'ai_memory_profile');

-- 检查ai_chat_message表外键约束
SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'ai_chat_message'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 检查ai_memory_profile表唯一索引
SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'ai_memory_profile';
