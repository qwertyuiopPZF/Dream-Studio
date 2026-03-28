ALTER TABLE `article`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

<<<<<<< HEAD
=======
ALTER TABLE `moment`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
ALTER TABLE `forum_post`
  ADD COLUMN IF NOT EXISTS `author_id` bigint DEFAULT NULL COMMENT '作者用户ID' AFTER `id`;

UPDATE `article`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;

<<<<<<< HEAD
=======
UPDATE `moment`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
UPDATE `forum_post`
SET `author_id` = (SELECT `id` FROM `user_account` WHERE `username` = 'admin' LIMIT 1)
WHERE `author_id` IS NULL;
