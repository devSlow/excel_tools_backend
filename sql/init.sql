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

-- 轮播图/公告表（统一管理轮播图和公告）
CREATE TABLE IF NOT EXISTS `banner` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `title`      VARCHAR(255) NOT NULL DEFAULT ''      COMMENT '标题',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '图片地址',
    `link_url`   VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '跳转链接',
    `content`    TEXT         DEFAULT NULL             COMMENT '公告内容，HTML富文本',
    `type`       VARCHAR(32)  NOT NULL DEFAULT 'info'  COMMENT '类型：info-一般通知/warning-警告/error-紧急',
    `sort_order` INT          NOT NULL DEFAULT 0       COMMENT '排序顺序',
    `status`     TINYINT      NOT NULL DEFAULT 1       COMMENT '状态 1:启用 0:禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图/公告表';

CREATE TABLE IF NOT EXISTS `app_config` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key`     VARCHAR(128) NOT NULL DEFAULT ''      COMMENT '配置键',
    `config_value`   VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '配置值',
    `update_content` TEXT         DEFAULT NULL             COMMENT '更新公告内容',
    `update_image`   VARCHAR(512) NOT NULL DEFAULT ''      COMMENT '更新公告插图',
    `remark`         VARCHAR(255) NOT NULL DEFAULT ''      COMMENT '备注',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用配置表';

INSERT INTO `app_config` (`config_key`, `config_value`, `update_content`, `update_image`, `remark`)
VALUES ('app_version', '1.1.0', '', '', '小程序版本号')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
