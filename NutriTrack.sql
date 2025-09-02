/*
 Navicat Premium Dump SQL

 Source Server         : tiger
 Source Server Type    : MySQL
 Source Server Version : 50731 (5.7.31-log)
 Source Host           : localhost:3306
 Source Schema         : nutritrack

 Target Server Type    : MySQL
 Target Server Version : 50731 (5.7.31-log)
 File Encoding         : 65001

 Date: 02/09/2025 17:42:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for daily_diet_analysis
-- ----------------------------
DROP TABLE IF EXISTS `daily_diet_analysis`;
CREATE TABLE `daily_diet_analysis`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分析报告的唯一ID',
  `user_id` bigint(20) NOT NULL COMMENT '关联的用户ID',
  `analysis_date` date NOT NULL COMMENT '分析的日期',
  `health_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '健康度评分 (例如 0-100.00)',
  `carb_grams` decimal(10, 2) NULL DEFAULT NULL COMMENT '碳水化合物总克数',
  `protein_grams` decimal(10, 2) NULL DEFAULT NULL COMMENT '蛋白质总克数',
  `fat_grams` decimal(10, 2) NULL DEFAULT NULL COMMENT '脂肪总克数',
  `nutrition_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '营养文字分析总结',
  `health_suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '宏观健康建议',
  `improvement_suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '具体饮食改进建议',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '报告更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id`, `analysis_date`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_analysis_date`(`analysis_date`) USING BTREE,
  CONSTRAINT `fk_daily_diet_analysis_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户的每日饮食汇总分析报告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_diet_analysis
-- ----------------------------
INSERT INTO `daily_diet_analysis` VALUES (1, 4, '2025-09-01', 75.50, 180.50, 95.20, 70.80, '您今天的早餐和午餐营养均衡，但晚餐的热量和脂肪摄入较高，导致全天总热量略超标。蛋白质摄入充足，值得表扬。', '建议在享受聚餐的同时，注意选择清淡一些的锅底和配菜，可以适当减少高脂肪肉类的摄入。保持日常的运动习惯，有助于热量消耗。', '晚餐时可以尝试用蔬菜或菌菇类代替部分肉类，增加饱腹感的同时降低脂肪摄入。如果感觉饥饿，可以选择一些低糖水果作为餐后补充。', '2025-09-02 17:05:56', '2025-09-02 17:05:56');

-- ----------------------------
-- Table structure for meal_images
-- ----------------------------
DROP TABLE IF EXISTS `meal_images`;
CREATE TABLE `meal_images`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片唯一ID',
  `record_id` bigint(20) NOT NULL COMMENT '关联的饮食记录ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片存储的URL',
  `upload_order` int(11) NOT NULL DEFAULT 0 COMMENT '图片排序，用于前端展示',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`record_id`) USING BTREE,
  CONSTRAINT `fk_meal_images_meal_records` FOREIGN KEY (`record_id`) REFERENCES `meal_records` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '饮食记录的图片' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_images
-- ----------------------------
INSERT INTO `meal_images` VALUES (5, 1, '1_20250901_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (6, 1, '1_20250901_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (7, 1, '1_20250901_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (8, 2, '2_20250901_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (9, 2, '2_20250901_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (10, 2, '2_20250901_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (11, 3, '3_20250901_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (12, 3, '3_20250901_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (13, 3, '3_20250901_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (14, 4, '4_20250901_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (15, 4, '4_20250901_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (16, 4, '4_20250901_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (17, 5, '5_20250902_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (18, 5, '5_20250902_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (19, 5, '5_20250902_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (20, 6, '6_20250902_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (21, 6, '6_20250902_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (22, 6, '6_20250902_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (23, 7, '7_20250902_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (24, 7, '7_20250902_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (25, 7, '7_20250902_3.jpg', 3, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (26, 8, '8_20250902_1.jpg', 1, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (27, 8, '8_20250902_2.jpg', 2, '2025-09-02 17:34:07');
INSERT INTO `meal_images` VALUES (28, 8, '8_20250902_3.jpg', 3, '2025-09-02 17:34:07');

-- ----------------------------
-- Table structure for meal_records
-- ----------------------------
DROP TABLE IF EXISTS `meal_records`;
CREATE TABLE `meal_records`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录唯一ID',
  `user_id` bigint(20) NOT NULL COMMENT '关联的用户ID',
  `meal_type` enum('breakfast','lunch','dinner','snack') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '餐别 (早餐, 午餐, 晚餐, 加餐)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户填写的文字描述',
  `calories` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '估算的热量（单位：大卡 kcal）',
  `rating` decimal(2, 1) NULL DEFAULT NULL COMMENT '用户评分 (例如 4.5, 5.0)',
  `record_date` date NOT NULL COMMENT '记录的日期',
  `is_shared` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否分享到社区 (0:不分享, 1:分享)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE,
  INDEX `idx_is_shared`(`is_shared`) USING BTREE,
  CONSTRAINT `fk_meal_records_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户饮食记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_records
-- ----------------------------
INSERT INTO `meal_records` VALUES (1, 4, 'breakfast', '一杯燕麦拿铁，两片全麦面包，一个煎蛋。开启元气满满的一天！', 350, 4.5, '2025-09-01', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (2, 4, 'lunch', '公司楼下的轻食沙拉，主要是鸡胸肉和蔬菜。', 450, 4.0, '2025-09-01', 0, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (3, 4, 'dinner', '晚上和朋友聚餐，吃了寿喜锅，有点放纵了~', 950, 5.0, '2025-09-01', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (4, 5, 'dinner', '自己做的番茄意面，简单又健康。', 550, 4.5, '2025-09-01', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (5, 4, 'breakfast', '今天起晚了，只喝了一杯黑咖啡。', 20, 3.0, '2025-09-02', 0, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (6, 4, 'lunch', '点的外卖麻辣香锅，好吃是好吃，但感觉热量爆炸。', 880, 4.0, '2025-09-02', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (7, 5, 'breakfast', '自制牛油果奶昔，好喝！', 320, 5.0, '2025-09-02', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `meal_records` VALUES (8, 6, 'lunch', '健身后的午餐，三文鱼、西兰花和糙米饭，非常标准的健身餐。', 580, 5.0, '2025-09-02', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知唯一ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收通知的用户ID',
  `sender_user_id` bigint(20) NOT NULL COMMENT '触发通知的用户ID (系统消息时为系统用户ID)',
  `notification_type` enum('meal_reminder','system','like','comment') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型',
  `target_record_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的饮食记录ID, 用于跳转',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容摘要',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读 (0:未读, 1:已读)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id_is_read`(`user_id`, `is_read`) USING BTREE,
  INDEX `idx_sender_user_id`(`sender_user_id`) USING BTREE,
  INDEX `idx_target_record_id`(`target_record_id`) USING BTREE,
  CONSTRAINT `fk_notifications_meal_records` FOREIGN KEY (`target_record_id`) REFERENCES `meal_records` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_notifications_receiver_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_notifications_sender_users` FOREIGN KEY (`sender_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通用用户通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, 4, 5, 'like', 3, '皮皮酱 赞了你的记录', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (2, 4, 7, 'like', 3, '月之泣 赞了你的记录', 1, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (3, 5, 4, 'like', 4, '小王几 赞了你的记录', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (4, 6, 4, 'like', 8, '小王几 赞了你的记录', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (5, 6, 5, 'like', 8, '皮皮酱 赞了你的记录', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (6, 4, 5, 'comment', 3, '皮皮酱 评论了你：看起来也太好吃了吧！哪家店呀？', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (7, 6, 9, 'comment', 8, '黑金派 评论了你：太自律了！求一份详细的食谱参考下。', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (8, 8, 1, 'system', NULL, '欢迎加入NutriTrack！开始记录你的第一餐，追踪健康生活吧！', 0, '2025-09-02 17:05:56');
INSERT INTO `notifications` VALUES (9, 4, 2, 'meal_reminder', NULL, '晚餐时间到啦，别忘了记录今天吃了什么哦~', 0, '2025-09-02 17:05:56');

-- ----------------------------
-- Table structure for record_comments
-- ----------------------------
DROP TABLE IF EXISTS `record_comments`;
CREATE TABLE `record_comments`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
  `record_id` bigint(20) NOT NULL COMMENT '被评论的饮食记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论的用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `parent_comment_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的父评论ID，NULL为一级评论',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`record_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_parent_comment_id`(`parent_comment_id`) USING BTREE,
  CONSTRAINT `fk_record_comments_meal_records` FOREIGN KEY (`record_id`) REFERENCES `meal_records` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_comments_parent_comment` FOREIGN KEY (`parent_comment_id`) REFERENCES `record_comments` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_comments_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '饮食记录评论表（支持楼中楼）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of record_comments
-- ----------------------------
INSERT INTO `record_comments` VALUES (1, 3, 5, '看起来也太好吃了吧！哪家店呀？', NULL, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `record_comments` VALUES (2, 3, 4, '就在公司附近新开的那家哦，下次一起去！', 1, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `record_comments` VALUES (3, 8, 9, '太自律了！求一份详细的食谱参考下。', NULL, '2025-09-02 17:05:56', '2025-09-02 17:05:56');
INSERT INTO `record_comments` VALUES (4, 8, 6, '没问题，我晚点整理一下发给你~', 3, '2025-09-02 17:05:56', '2025-09-02 17:05:56');

-- ----------------------------
-- Table structure for record_likes
-- ----------------------------
DROP TABLE IF EXISTS `record_likes`;
CREATE TABLE `record_likes`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞记录唯一ID',
  `record_id` bigint(20) NOT NULL COMMENT '被点赞的饮食记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞的用户ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_record`(`user_id`, `record_id`) USING BTREE COMMENT '联合唯一索引，防止重复点赞',
  INDEX `idx_record_id`(`record_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_record_likes_meal_records` FOREIGN KEY (`record_id`) REFERENCES `meal_records` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_likes_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '饮食记录点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of record_likes
-- ----------------------------
INSERT INTO `record_likes` VALUES (1, 3, 5, '2025-09-02 17:05:56');
INSERT INTO `record_likes` VALUES (2, 3, 7, '2025-09-02 17:05:56');
INSERT INTO `record_likes` VALUES (3, 4, 4, '2025-09-02 17:05:56');
INSERT INTO `record_likes` VALUES (4, 8, 4, '2025-09-02 17:05:56');
INSERT INTO `record_likes` VALUES (5, 8, 5, '2025-09-02 17:05:56');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，用于登录',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '重要：存储Bcrypt等算法加密后的密码哈希值，严禁存储明文！',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱，可用于登录或找回密码',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号，可用于登录或找回密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称，在社区显示',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像图片URL',
  `status` enum('active','inactive','suspended','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '用户状态: active-活跃, inactive-未激活, suspended-被封禁, deleted-已注销',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息更新时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '执行注销操作的时间，status不为deleted时为NULL',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email`) USING BTREE,
  UNIQUE INDEX `uk_phone_number`(`phone_number`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表（包含软删除逻辑）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'system', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '系统通知', 'https://i.pravatar.cc/150?u=system', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (2, 'reminder', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '用餐提醒', 'https://i.pravatar.cc/150?u=reminder', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (3, 'suggestion', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '健康建议', 'https://i.pravatar.cc/150?u=suggestion', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (4, 'xwj', '$2a$10$ejkSpdnmxFRYnS2PIfinM.tbSesjCaOV1rUT.f9hEMWULkeNj6FUS', 'xwj@example.com', '13800138001', '小王几', 'https://i.pravatar.cc/150?u=xwj', 'active', '2025-09-02 17:05:56', '2025-09-02 17:07:52', NULL);
INSERT INTO `users` VALUES (5, 'pj', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'pj@example.com', '13800138002', '皮皮酱', 'https://i.pravatar.cc/150?u=pj', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (6, 'yht', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'yht@example.com', '13800138003', '樱花糖', 'https://i.pravatar.cc/150?u=yht', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (7, 'yzq', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'yzq@example.com', '13800138004', '月之泣', 'https://i.pravatar.cc/150?u=yzq', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (8, 'ysj', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'ysj@example.com', '13800138005', '艺术家', 'https://i.pravatar.cc/150?u=ysj', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);
INSERT INTO `users` VALUES (9, 'hjp', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'hjp@example.com', '13800138006', '黑金派', 'https://i.pravatar.cc/150?u=hjp', 'active', '2025-09-02 17:05:56', '2025-09-02 17:05:56', NULL);

SET FOREIGN_KEY_CHECKS = 1;
