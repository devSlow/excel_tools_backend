-- Excel Tools 数据库完整初始化脚本
-- MySQL 5.7+ 兼容（JSON 类型需要 MySQL 5.7.8+）
-- 字符集：utf8mb4 支持完整中文及 emoji

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `excel_tools`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `excel_tools`;

-- ==================== 用户表 ====================
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid`     VARCHAR(128) NOT NULL DEFAULT ''    COMMENT '微信openid',
    `nickname`   VARCHAR(64)  NOT NULL DEFAULT ''    COMMENT '昵称',
    `avatar`     VARCHAR(512) NOT NULL DEFAULT ''    COMMENT '头像URL',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==================== 任务表 ====================
CREATE TABLE IF NOT EXISTS `task` (
    `id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id`    BIGINT(20)   NOT NULL DEFAULT 0     COMMENT '所属用户ID',
    `title`      VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '任务标题',
    `columns`    JSON          DEFAULT NULL           COMMENT '列定义，如：[{"name":"姓名","type":"text"}]',
    `rows`       JSON          DEFAULT NULL           COMMENT '行数据，如：[{"姓名":"张三"}]',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据任务表';

-- ==================== 轮播图/公告表 ====================
CREATE TABLE IF NOT EXISTS `banner` (
    `id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `title`      VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '标题',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '图片地址',
    `link_url`   VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '跳转链接',
    `content`    TEXT          DEFAULT NULL           COMMENT '公告内容，HTML富文本',
    `type`       VARCHAR(32)  NOT NULL DEFAULT 'info' COMMENT '类型：info-一般通知/warning-警告/error-紧急',
    `sort_order` INT(11)      NOT NULL DEFAULT 0     COMMENT '排序顺序，数字越大越靠前',
    `status`     TINYINT(4)   NOT NULL DEFAULT 1     COMMENT '状态 1:启用 0:禁用',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图/公告表';

-- ==================== 公告表 ====================
CREATE TABLE IF NOT EXISTS `notice` (
    `id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`      VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '标题',
    `content`    TEXT          DEFAULT NULL           COMMENT '内容，支持HTML富文本',
    `priority`   INT(11)      NOT NULL DEFAULT 0     COMMENT '优先级 0:普通 1:重要 2:紧急',
    `status`     TINYINT(4)   NOT NULL DEFAULT 1     COMMENT '状态 1:启用 0:禁用',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ==================== 应用配置表 ====================
CREATE TABLE IF NOT EXISTS `app_config` (
    `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key`     VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '配置键',
    `config_value`   VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '配置值',
    `update_content` TEXT          DEFAULT NULL           COMMENT '更新公告内容',
    `update_image`   VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '更新公告插图',
    `remark`         VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '备注',
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用配置表';

-- ==================== 初始数据 ====================

-- 应用配置初始数据
INSERT INTO `app_config` (`config_key`, `config_value`, `update_content`, `update_image`, `remark`)
VALUES ('app_version', '1.1.0', '', '', '小程序版本号')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- 示例：插入一条默认公告
INSERT INTO `notice` (`title`, `content`, `priority`, `status`)
VALUES ('欢迎使用Excel工具', '<p>欢迎使用Excel智能工具小程序！</p><p>主要功能：</p><ul><li>文本智能解析</li><li>Excel多Sheet导入</li><li>数据分组导出</li></ul>', 0, 1);

-- 示例：插入一条轮播图数据
INSERT INTO `banner` (`title`, `image_url`, `link_url`, `content`, `type`, `sort_order`, `status`)
VALUES ('Excel工具使用教程', 'https://example.com/banner1.png', '/pages/guide/index', '<h3>快速上手</h3><p>点击查看使用教程</p>', 'info', 1, 1);
