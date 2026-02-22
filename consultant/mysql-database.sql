-- 创建数据库
CREATE DATABASE IF NOT EXISTS volunteer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE volunteer;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `avatar` VARCHAR(255) COMMENT '用户头像URL',
    INDEX idx_username (`username`),
    INDEX idx_email (`email`),
    INDEX idx_phone (`phone`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
                                              `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                              `title` VARCHAR(255) NOT NULL COMMENT '会话标题',
    `memory_id` VARCHAR(100) COMMENT '会话记忆ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_user_id (`user_id`),
    INDEX idx_memory_id (`memory_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
                                         `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
                                         `role` VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-助手',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_conversation_id (`conversation_id`),
    INDEX idx_role (`role`),
    FOREIGN KEY (`conversation_id`) REFERENCES `conversation`(`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 日志事件表
CREATE TABLE IF NOT EXISTS `log_event` (
                                           `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           `timestamp` DATETIME NOT NULL,
                                           `level` VARCHAR(20) NOT NULL,
    `logger` VARCHAR(255) NOT NULL,
    `message` TEXT,
    `thread` VARCHAR(100),
    `class_name` VARCHAR(255),
    `method_name` VARCHAR(100),
    `line_number` INT,
    `request_url` VARCHAR(255),
    `request_method` VARCHAR(20),
    `ip_address` VARCHAR(50),
    `user_id` BIGINT,
    `exception` TEXT,
    `trace_id` VARCHAR(100),
    INDEX idx_timestamp (`timestamp`),
    INDEX idx_level (`level`),
    INDEX idx_user_id (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;