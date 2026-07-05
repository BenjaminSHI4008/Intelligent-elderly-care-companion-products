-- 消息撤回字段
ALTER TABLE `family_message`
  ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常 1=已撤回' AFTER `is_read`,
  ADD COLUMN `recalled_at` DATETIME NULL COMMENT '撤回时间' AFTER `status`;
