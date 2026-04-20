-- 新增商品评分表
CREATE TABLE `product_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rating` TINYINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_user` (`product_id`, `user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- product 表新增评分冗余字段
ALTER TABLE `product` ADD COLUMN `avg_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '商品平均评分';
ALTER TABLE `product` ADD COLUMN `rating_count` INT NOT NULL DEFAULT 0 COMMENT '评分总人数';
