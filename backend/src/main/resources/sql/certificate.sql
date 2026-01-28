-- 创建证件表（实名认证表）
CREATE TABLE IF NOT EXISTS `certificate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证件ID，主键，自增',
    `certificate_number` VARCHAR(100) NOT NULL COMMENT '证件号，唯一索引',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN-管理员，USER-普通用户，MERCHANT-商家',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_certificate_number` (`certificate_number`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件实名认证表';

-- 插入测试数据 - 管理员证件
INSERT INTO `certificate` (`certificate_number`, `name`, `role`) VALUES
('ADMIN0000', '系统管理员', 'ADMIN'),
('ADMIN0001', '超级管理员', 'ADMIN');

-- 插入测试数据 - 商家证件
INSERT INTO `certificate` (`certificate_number`, `name`, `role`) VALUES
('MERCHANT0001', '商家1', 'MERCHANT'),
('MERCHANT0002', '商家2', 'MERCHANT'),
('MERCHANT0003', '商家3', 'MERCHANT'),
('MERCHANT0004', '商家4', 'MERCHANT'),
('MERCHANT0005', '商家5', 'MERCHANT');

-- 插入测试数据 - 普通用户证件
INSERT INTO `certificate` (`certificate_number`, `name`, `role`) VALUES
('USER0001', '用户1', 'USER'),
('USER0002', '用户2', 'USER'),
('USER0003', '用户3', 'USER'),
('USER0004', '用户4', 'USER'),
('USER0005', '用户5', 'USER');