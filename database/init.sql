-- ============================================
-- 「小伴」MVP数据库初始化脚本
-- 数据库：xiaoban_mvp
-- 字符集：utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS `xiaoban_mvp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `xiaoban_mvp`;

-- 表1：用户表
CREATE TABLE `user` (
  `user_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号（登录凭证）',
  `password_hash` VARCHAR(128) NOT NULL COMMENT '密码哈希',
  `role` ENUM('elder', 'child') NOT NULL COMMENT '角色：elder老人/child子女',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称（老人端显示称谓如"妈妈"）',
  `gender` VARCHAR(10) DEFAULT '' COMMENT '性别',
  `birthday` VARCHAR(10) DEFAULT '' COMMENT '生日 yyyy-MM-dd',
  `avatar_url` VARCHAR(255) DEFAULT '' COMMENT '头像URL',
  `device_model` VARCHAR(100) DEFAULT '' COMMENT '设备型号',
  `last_active_at` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_phone` (`phone`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 表2：设备绑定关系表
CREATE TABLE `binding_relation` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `elder_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `child_id` BIGINT NOT NULL COMMENT '子女用户ID',
  `bind_type` ENUM('bluetooth', 'code') NOT NULL COMMENT '绑定方式',
  `bindcode` VARCHAR(6) DEFAULT NULL COMMENT '绑定时使用的6位码',
  `status` ENUM('active', 'unbound') DEFAULT 'active' COMMENT '绑定状态',
  `bind_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`elder_id`) REFERENCES `user`(`user_id`),
  FOREIGN KEY (`child_id`) REFERENCES `user`(`user_id`),
  UNIQUE KEY `uk_elder_child` (`elder_id`, `child_id`),
  INDEX `idx_elder` (`elder_id`),
  INDEX `idx_child` (`child_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备绑定关系表（多子女可绑同一老人）';

-- 表3：对话记录表
CREATE TABLE `conversation` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `elder_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID（同一次连续对话共享）',
  `user_question` TEXT NOT NULL COMMENT '老人原话（语音转文字结果）',
  `ai_answer` TEXT NOT NULL COMMENT 'AI回答文本',
  `category` ENUM('normal', 'health', 'urgent') DEFAULT 'normal' COMMENT '问题分类',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT '私密模式：0正常 1私密（不同步子女）',
  `confirm_status` ENUM('none', 'pending', 'confirmed', 'corrected') DEFAULT 'none' COMMENT '子女确认状态',
  `child_correction` TEXT DEFAULT NULL COMMENT '子女纠正内容',
  `corrected_by` BIGINT DEFAULT NULL COMMENT '纠正人（子女user_id）',
  `corrected_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`elder_id`) REFERENCES `user`(`user_id`),
  INDEX `idx_elder_date` (`elder_id`, `created_at`),
  INDEX `idx_confirm` (`confirm_status`),
  INDEX `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- 表4：远程提醒表
CREATE TABLE `reminder` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `child_id` BIGINT NOT NULL COMMENT '设置提醒的子女ID',
  `elder_id` BIGINT NOT NULL COMMENT '被提醒的老人ID',
  `content` VARCHAR(200) NOT NULL COMMENT '提醒内容（如：吃降压药）',
  `remind_time` TIME NOT NULL COMMENT '每日提醒时间',
  `repeat_type` ENUM('daily', 'once') DEFAULT 'daily' COMMENT '重复类型',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`child_id`) REFERENCES `user`(`user_id`),
  FOREIGN KEY (`elder_id`) REFERENCES `user`(`user_id`),
  INDEX `idx_elder_active` (`elder_id`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='远程提醒表';

-- 表5：家庭消息表
CREATE TABLE `family_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `msg_type` ENUM('text', 'voice', 'photo') NOT NULL COMMENT '消息类型',
  `content` TEXT DEFAULT NULL COMMENT '文字内容',
  `media_url` VARCHAR(255) DEFAULT NULL COMMENT '语音/照片文件URL',
  `duration` INT DEFAULT NULL COMMENT '语音时长（秒）',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`sender_id`) REFERENCES `user`(`user_id`),
  FOREIGN KEY (`receiver_id`) REFERENCES `user`(`user_id`),
  INDEX `idx_receiver_read` (`receiver_id`, `is_read`),
  INDEX `idx_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭消息表';

-- 表6：智能日报表
CREATE TABLE `daily_report` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `elder_id` BIGINT NOT NULL COMMENT '老人ID',
  `report_date` DATE NOT NULL COMMENT '日报日期',
  `total_conversations` INT DEFAULT 0 COMMENT '当日对话总数',
  `summary_text` TEXT COMMENT 'AI生成的对话摘要',
  `health_flags` JSON DEFAULT NULL COMMENT '健康关注项（JSON数组）',
  `mood_score` TINYINT DEFAULT 3 COMMENT '情绪评分1-5',
  `topic_distribution` JSON DEFAULT NULL COMMENT '话题分布（JSON对象）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`elder_id`) REFERENCES `user`(`user_id`),
  UNIQUE KEY `uk_elder_date` (`elder_id`, `report_date`),
  INDEX `idx_elder_date` (`elder_id`, `report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能日报表';

-- 表7：预警词库表
CREATE TABLE `alert_keyword` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `keyword` VARCHAR(50) NOT NULL COMMENT '预警关键词',
  `category` ENUM('health', 'urgent', 'emotional') NOT NULL COMMENT '分类',
  `priority` ENUM('high', 'medium', 'low') DEFAULT 'medium' COMMENT '优先级',
  `is_active` TINYINT(1) DEFAULT 1,
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警关键词库';

-- 表8：绑定码临时表
CREATE TABLE `bind_code_temp` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `elder_id` BIGINT NOT NULL,
  `code` VARCHAR(6) NOT NULL COMMENT '6位绑定码',
  `expire_at` DATETIME NOT NULL COMMENT '过期时间（生成后5分钟）',
  `is_used` TINYINT(1) DEFAULT 0,
  INDEX `idx_code` (`code`),
  INDEX `idx_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绑定码临时表';

-- ============================================
-- 初始数据：预警关键词
-- ============================================
INSERT INTO `alert_keyword` (`keyword`, `category`, `priority`) VALUES
('疼', 'health', 'high'),
('痛', 'health', 'high'),
('摔了', 'urgent', 'high'),
('摔倒', 'urgent', 'high'),
('头晕', 'health', 'high'),
('胸闷', 'health', 'high'),
('不舒服', 'health', 'medium'),
('忘了吃药', 'health', 'medium'),
('喘不上气', 'urgent', 'high'),
('看不清', 'health', 'medium'),
('不想吃饭', 'emotional', 'low'),
('睡不着', 'health', 'medium'),
('心慌', 'health', 'high'),
('手抖', 'health', 'medium'),
('没力气', 'health', 'medium'),
('难受', 'health', 'medium'),
('孤单', 'emotional', 'low'),
('想你们', 'emotional', 'low');
