-- Excel Tools 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS `excel_tools`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `excel_tools`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid`     VARCHAR(128) NOT NULL DEFAULT ''      COMMENT '微信openid',
    `nickname`   VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '昵称',
    `avatar`     VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '头像URL',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 任务表
CREATE TABLE IF NOT EXISTS `task` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id`    BIGINT       NOT NULL DEFAULT 0       COMMENT '所属用户ID',
    `title`      VARCHAR(255) NOT NULL DEFAULT ''      COMMENT '任务标题',
    `columns`    JSON         DEFAULT NULL             COMMENT '列定义，如：[{"name":"姓名","type":"text"}]',
    `rows`       JSON         DEFAULT NULL             COMMENT '行数据，如：[{"姓名":"张三"}]',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据任务表';
