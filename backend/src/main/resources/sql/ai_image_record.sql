CREATE TABLE IF NOT EXISTS `ai_image_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `original_image_url` TEXT DEFAULT NULL,
    `result_image_url` TEXT NOT NULL,
    `prompt` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
