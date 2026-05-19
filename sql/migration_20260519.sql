-- 数据库迁移脚本 2026-05-19
-- 在远程数据库执行前请先备份

USE `excel_tools`;

-- ==================== 1. banner 表：添加 notice_id 列 ====================
-- 实体 Banner.java 有 noticeId 字段，但原建表语句缺少此列
ALTER TABLE `banner`
  ADD COLUMN `notice_id` BIGINT(20) DEFAULT NULL COMMENT '关联公告ID' AFTER `link_url`;

-- ==================== 2. redeem_code 表：新建 ====================
-- 实体 RedeemCode.java 映射此表，原建表语句中缺失
CREATE TABLE IF NOT EXISTS `redeem_code` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plain_code`  VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '明文兑换码',
    `code`        VARCHAR(256) NOT NULL DEFAULT ''     COMMENT 'SHA-256哈希值',
    `salt`        VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '盐值',
    `max_usage`   INT(11)      NOT NULL DEFAULT 1      COMMENT '最大使用次数',
    `used_count`  INT(11)      NOT NULL DEFAULT 0      COMMENT '已使用次数',
    `status`      TINYINT(4)   NOT NULL DEFAULT 1      COMMENT '状态 1:启用 0:禁用',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `expires_at`  DATETIME      DEFAULT NULL           COMMENT '过期时间',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码表';
