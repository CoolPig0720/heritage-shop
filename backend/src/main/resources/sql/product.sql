CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `merchant_id` BIGINT NOT NULL,
    `trace_code` VARCHAR(100) DEFAULT NULL,
    `trace_qr_url` VARCHAR(255) DEFAULT NULL,
    `model_3d_url` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT(1) NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_cover` TINYINT(1) NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_product_cover` (`product_id`, `is_cover`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `product` (`name`, `description`, `price`, `merchant_id`, `trace_code`, `trace_qr_url`, `model_3d_url`, `status`) VALUES
('手工剪纸艺术画', '传统剪纸工艺，手工制作，适合作为家居装饰。', 299.00, 1, 'TRACE20260127001', '/uploads/trace-qr-1.png', '/uploads/model-1.glb', 1),
('传统蜡染围巾', '天然染料蜡染工艺，色彩自然，质感细腻。', 599.00, 1, 'TRACE20260127002', '/uploads/trace-qr-2.png', NULL, 1),
('景泰蓝花瓶', '景泰蓝传统工艺，色泽明丽，适合收藏。', 1299.00, 2, NULL, NULL, '/uploads/model-3.gltf', 1),
('苏绣团扇', '苏绣手工刺绣团扇，花纹精致。', 399.00, 2, 'TRACE20260127003', NULL, NULL, 0);

INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`, `is_cover`) VALUES
(1, '/uploads/product-1-cover.png', 0, 1),
(1, '/uploads/product-1-1.png', 1, 0),
(1, '/uploads/product-1-2.png', 2, 0),
(2, '/uploads/product-2-cover.png', 0, 1),
(2, '/uploads/product-2-1.png', 1, 0),
(3, '/uploads/product-3-cover.png', 0, 1),
(3, '/uploads/product-3-1.png', 1, 0),
(4, '/uploads/product-4-cover.png', 0, 1);
