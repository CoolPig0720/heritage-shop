DROP TABLE IF EXISTS `comment_like`;
DROP TABLE IF EXISTS `product_comment`;

CREATE TABLE `product_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `reply_to_user_id` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `like_count` INT NOT NULL DEFAULT 0,
    `status` TINYINT(1) NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_status` (`product_id`, `status`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `comment_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `comment_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
