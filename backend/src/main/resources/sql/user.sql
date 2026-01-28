-- 创建数据库
CREATE DATABASE IF NOT EXISTS `develop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `develop`;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键，自增',
    `account` VARCHAR(50) NOT NULL COMMENT '账号，用于登录，唯一索引',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，BCrypt 加密',
    `name` VARCHAR(50) NOT NULL COMMENT '名称，用户可修改',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户，MERCHANT-商家',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `certificate_number` VARCHAR(100) DEFAULT NULL COMMENT '证件号，实名认证后填充，唯一索引',
    `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`account`),
    UNIQUE KEY `uk_certificate_number` (`certificate_number`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建证件表
CREATE TABLE IF NOT EXISTS `certificate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证件ID，主键，自增',
    `certificate_number` VARCHAR(100) NOT NULL COMMENT '证件号，唯一索引',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：USER-普通用户，MERCHANT-商家',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_certificate_number` (`certificate_number`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件表';

-- 插入测试数据 - 证件表预置数据
INSERT INTO `certificate` (`certificate_number`, `name`, `role`) VALUES
('MERCHANT001', '张三', 'MERCHANT'),
('MERCHANT002', '李四', 'MERCHANT'),
('MERCHANT003', '王五', 'MERCHANT'),
('USER001', '赵六', 'USER'),
('USER002', '钱七', 'USER'),
('USER003', '孙八', 'USER');

-- 插入测试数据 - 管理员账号（密码：admin123，使用 BCrypt 加密）
INSERT INTO `user` (`account`, `password`, `name`, `role`, `avatar`, `certificate_number`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', NULL, NULL);
