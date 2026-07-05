USE `xiaoban_mvp`;

ALTER TABLE `user`
  ADD COLUMN `emergency_contact` VARCHAR(20) DEFAULT '' COMMENT '紧急联系人电话' AFTER `birthday`;
