DROP TABLE IF EXISTS `heritage_story`;
DROP TABLE IF EXISTS `announcement`;

CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-发布，0-草稿',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `heritage_story` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `heritage_project_id` BIGINT DEFAULT NULL COMMENT '关联非遗项目ID（可选）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-发布，0-草稿',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_heritage_project_id` (`heritage_project_id`),
    KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `announcement` (`title`, `content`, `status`, `is_top`) VALUES
('非遗商城正式上线', '<p>尊敬的用户，非遗文化电商平台现已正式上线！我们致力于为您带来最优质的非遗文化产品与体验，欢迎浏览和选购。</p>', 1, 1),
('关于春节期间物流延迟的公告', '<p>春节期间（1月28日-2月4日），物流配送将有所延迟，敬请谅解。节后恢复正常发货。</p>', 1, 0),
('平台入驻商家招募中', '<p>我们诚邀全国各地的非遗传承人和手工艺人入驻平台，共同推广非遗文化。有意者请联系官方邮箱。</p>', 0, 0);

INSERT INTO `heritage_story` (`title`, `content`, `heritage_project_id`, `status`, `is_top`) VALUES
('苏绣：针尖上的千年传承', '<p>苏绣是中国优秀的民族传统工艺之一，是苏州地区刺绣产品的总称。苏绣具有图案秀丽、构思巧妙、绣工细致、针法活泼、色彩清雅的独特风格，地方特色浓郁。</p><p>苏绣以苏州刺绣研究所的高新区的镇湖街道（苏绣小镇）刺绣最为有名。镇湖街道是苏绣的主要发源地，苏绣中的八成产品来自镇湖。</p><p><img src="/uploads/heritage/project-1-display-1.jpg" alt="苏绣作品" style="max-width:100%"></p>', 1, 1, 1),
('蔚县剪纸：刀刻出的色彩世界', '<p>蔚县剪纸是用专业的刻刀刻制而成，而非传统剪纸用剪刀剪成。蔚县剪纸以"阴刻"和"色彩点染"为主，形成了一种独特的民间艺术风格。</p><p>蔚县剪纸已有两百多年的历史，其作者多为农民，他们以朴素的艺术思维、精湛的工艺技巧，创造出了五彩缤纷的剪纸艺术世界。</p>', 2, 1, 0),
('景德镇陶瓷的火与土之歌', '<p>景德镇是中国"瓷都"，有着1700多年的制瓷历史。景德镇陶瓷以"白如玉、明如镜、薄如纸、声如磬"著称，其青花瓷、粉彩瓷、玲珑瓷、颜色釉瓷合称四大名瓷。</p>', NULL, 1, 0);
