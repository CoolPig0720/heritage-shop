DROP TABLE IF EXISTS `customize_message`;
DROP TABLE IF EXISTS `customize_request`;

CREATE TABLE `customize_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `merchant_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT NOT NULL,
    `image_urls` JSON DEFAULT NULL COMMENT '用户上传的参考图片URL列表(最多3张)',
    `quoted_price` DECIMAL(10,2) DEFAULT NULL,
    `address_id` BIGINT DEFAULT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `order_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_merchant_status` (`merchant_id`, `status`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `customize_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `request_id` BIGINT NOT NULL,
    `sender_id` BIGINT NOT NULL,
    `sender_type` VARCHAR(10) NOT NULL,
    `content` TEXT NOT NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=未读, 1=已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_request_id` (`request_id`),
    KEY `idx_request_created` (`request_id`, `create_time`),
    KEY `idx_sender_read` (`sender_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
