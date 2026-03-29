ALTER TABLE `article`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

ALTER TABLE `moment`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

ALTER TABLE `forum_post`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

UPDATE `article`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;

UPDATE `moment`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;

UPDATE `forum_post`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;
