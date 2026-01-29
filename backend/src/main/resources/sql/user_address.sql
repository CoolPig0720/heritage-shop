DROP TABLE IF EXISTS `user_address`;

CREATE TABLE `user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `receiver_name` VARCHAR(50) NOT NULL,
    `receiver_phone` VARCHAR(20) NOT NULL,
    `region_code_path` VARCHAR(512) DEFAULT NULL COMMENT '地区编码路径，/ 分隔（可空以支持海外）',
    `region_name_path` VARCHAR(512) NOT NULL COMMENT '地区名称路径，/ 分隔',
    `region_depth` INT NOT NULL DEFAULT 0 COMMENT '地区层级深度',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '冗余：第1级名称',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '冗余：第2级名称',
    `district` VARCHAR(50) DEFAULT NULL COMMENT '冗余：第3级名称',
    `detail_address` VARCHAR(255) NOT NULL,
    `is_default` TINYINT(1) NOT NULL DEFAULT 0,
    `status` TINYINT(1) NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_default` (`user_id`, `is_default`),
    KEY `idx_user_region_depth` (`user_id`, `region_depth`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
