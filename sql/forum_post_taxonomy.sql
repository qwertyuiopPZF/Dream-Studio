ALTER TABLE `forum_post`
  ADD COLUMN IF NOT EXISTS `category_id` bigint DEFAULT NULL COMMENT '分类ID' AFTER `content`;

ALTER TABLE `forum_post`
  ADD COLUMN IF NOT EXISTS `tags` varchar(256) DEFAULT NULL COMMENT '标签ID列表，逗号分隔' AFTER `category_id`;

ALTER TABLE `forum_post`
  ADD INDEX `idx_forum_post_category_id` (`category_id`);
