DROP TABLE IF EXISTS `heritage_project_inheritor`;
DROP TABLE IF EXISTS `heritage_project_media`;
DROP TABLE IF EXISTS `heritage_inheritor`;
DROP TABLE IF EXISTS `heritage_project`;
DROP TABLE IF EXISTS `heritage_category`;

CREATE TABLE `heritage_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `heritage_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT NOT NULL,
    `apply_unit` VARCHAR(200) NOT NULL,
    `protect_unit` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `heritage_project_media` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `media_type` VARCHAR(50) NOT NULL,
    `media_url` VARCHAR(255) NOT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_project_type` (`project_id`, `media_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `heritage_inheritor` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `gender` VARCHAR(10) NOT NULL,
    `birth_date` DATE DEFAULT NULL,
    `nation` VARCHAR(50) DEFAULT NULL,
    `photo` VARCHAR(255) DEFAULT NULL,
    `description` TEXT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `heritage_project_inheritor` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `inheritor_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_inheritor` (`project_id`, `inheritor_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_inheritor_id` (`inheritor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `heritage_category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(1, '传统美术', NULL, 0),
(2, '传统技艺', NULL, 0),
(3, '刺绣', 2, 10),
(4, '剪纸', 1, 10);

INSERT INTO `heritage_project` (`id`, `category_id`, `name`, `description`, `apply_unit`, `protect_unit`) VALUES
(1, 3, '苏绣（刺绣）', '苏绣是中国优秀的传统刺绣技艺之一，针法细腻、构图典雅。', '苏州市文化和旅游局', '苏绣保护中心'),
(2, 4, '蔚县剪纸', '蔚县剪纸以刀刻为主，色彩鲜艳，题材丰富，具有浓郁地方特色。', '蔚县文化广电和旅游局', '蔚县剪纸保护中心');

INSERT INTO `heritage_project_media` (`project_id`, `media_type`, `media_url`, `sort_order`) VALUES
(1, 'DISPLAY_IMAGE', '/uploads/heritage/project-1-display-1.jpg', 0),
(1, 'PROCESS_IMAGE', '/uploads/heritage/project-1-process-1.jpg', 0),
(1, 'PROCESS_IMAGE', '/uploads/heritage/project-1-process-2.jpg', 1),
(1, 'VIDEO', '/uploads/heritage/project-1-video.mp4', 0),
(2, 'DISPLAY_IMAGE', '/uploads/heritage/project-2-display-1.jpg', 0),
(2, 'PROCESS_IMAGE', '/uploads/heritage/project-2-process-1.jpg', 0),
(2, 'VIDEO', '/uploads/heritage/project-2-video.mp4', 0);

INSERT INTO `heritage_inheritor` (`id`, `name`, `gender`, `birth_date`, `nation`, `photo`, `description`) VALUES
(1, '张三', '男', '1939-06-01', '汉族', '/uploads/heritage/inheritor-1.jpg', '长期从事苏绣技艺传承与教学。'),
(2, '李四', '女', '1955-03-12', NULL, '/uploads/heritage/inheritor-2.jpg', '蔚县剪纸代表性传承人。'),
(3, '王五', '男', NULL, NULL, NULL, NULL);

INSERT INTO `heritage_project_inheritor` (`project_id`, `inheritor_id`) VALUES
(1, 1),
(2, 2),
(1, 3);
