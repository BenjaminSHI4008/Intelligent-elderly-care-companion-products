USE `xiaoban_mvp`;

ALTER TABLE `user`
  ADD COLUMN `gender` VARCHAR(10) DEFAULT '' COMMENT '性别' AFTER `nickname`,
  ADD COLUMN `birthday` VARCHAR(10) DEFAULT '' COMMENT '生日 yyyy-MM-dd' AFTER `gender`;
