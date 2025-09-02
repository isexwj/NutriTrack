-- 创建数据库
CREATE DATABASE IF NOT EXISTS `NutriTrack` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `NutriTrack`;

-- -----------------------------------------------------
-- 表 1: `users` - 用户信息表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users`
(
    `id`           BIGINT                                              NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
    `username`     VARCHAR(50)                                         NOT NULL COMMENT '用户名，用于登录',
    `password`     VARCHAR(255)                                        NOT NULL COMMENT '重要：存储Bcrypt等算法加密后的密码哈希值，严禁存储明文！',
    `email`        VARCHAR(100)                                        NULL     DEFAULT NULL COMMENT '邮箱，可用于登录或找回密码',
    `phone_number` VARCHAR(20)                                         NULL     DEFAULT NULL COMMENT '手机号，可用于登录或找回密码',
    `nickname`     VARCHAR(50)                                         NULL     DEFAULT NULL COMMENT '用户昵称，在社区显示',
    `avatar_url`   VARCHAR(255)                                        NULL     DEFAULT NULL COMMENT '头像图片URL',
    `status`       ENUM ('active', 'inactive', 'suspended', 'deleted') NOT NULL DEFAULT 'active' COMMENT '用户状态: active-活跃, inactive-未激活, suspended-被封禁, deleted-已注销',
    `created_at`   TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`   TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息更新时间',
    `deleted_at`   TIMESTAMP                                           NULL     DEFAULT NULL COMMENT '执行注销操作的时间，status不为deleted时为NULL',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username` ASC),
    UNIQUE INDEX `uk_email` (`email` ASC),
    UNIQUE INDEX `uk_phone_number` (`phone_number` ASC),
    INDEX `idx_status` (`status` ASC)
) ENGINE = InnoDB COMMENT = '用户信息表（包含软删除逻辑）';


-- -----------------------------------------------------
-- 表 2: `meal_records` - 饮食记录表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `meal_records`;
CREATE TABLE IF NOT EXISTS `meal_records`
(
    `id`          BIGINT                                         NOT NULL AUTO_INCREMENT COMMENT '记录唯一ID',
    `user_id`     BIGINT                                         NOT NULL COMMENT '关联的用户ID',
    `meal_type`   ENUM ('breakfast', 'lunch', 'dinner', 'snack') NOT NULL COMMENT '餐别 (早餐, 午餐, 晚餐, 加餐)',
    `description` TEXT                                           NULL COMMENT '用户填写的文字描述',
    `calories`    INT UNSIGNED                                   NULL     DEFAULT NULL COMMENT '估算的热量（单位：大卡 kcal）',
    `rating`      DECIMAL(2, 1)                                  NULL     DEFAULT NULL COMMENT '用户评分 (例如 4.5, 5.0)',
    `record_date` DATE                                           NOT NULL COMMENT '记录的日期',
    `is_shared`   TINYINT(1)                                     NOT NULL DEFAULT 0 COMMENT '是否分享到社区 (0:不分享, 1:分享)',
    `created_at`  TIMESTAMP                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  TIMESTAMP                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id` ASC),
    INDEX `idx_record_date` (`record_date` ASC),
    INDEX `idx_is_shared` (`is_shared` ASC),
    CONSTRAINT `fk_meal_records_users`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '用户饮食记录表';


-- -----------------------------------------------------
-- 表 3: `meal_images` - 记录图片表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `meal_images`;
CREATE TABLE IF NOT EXISTS `meal_images`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片唯一ID',
    `record_id`    BIGINT       NOT NULL COMMENT '关联的饮食记录ID',
    `image_url`    VARCHAR(255) NOT NULL COMMENT '图片存储的URL',
    `upload_order` INT          NOT NULL DEFAULT 0 COMMENT '图片排序，用于前端展示',
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id` ASC),
    CONSTRAINT `fk_meal_images_meal_records`
        FOREIGN KEY (`record_id`)
            REFERENCES `meal_records` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '饮食记录的图片';


-- -----------------------------------------------------
-- 表 4: `daily_diet_analysis` - 每日饮食分析表
-- (注意：表名根据之前的对话已更新为 daily_diet_analysis)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `daily_diet_analysis`;
CREATE TABLE IF NOT EXISTS `daily_diet_analysis`
(
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '分析报告的唯一ID',
    `user_id`                 BIGINT         NOT NULL COMMENT '关联的用户ID',
    `analysis_date`           DATE           NOT NULL COMMENT '分析的日期',
    `health_score`            DECIMAL(5, 2)  NULL COMMENT '健康度评分 (例如 0-100.00)',
    `carb_grams`              DECIMAL(10, 2) NULL COMMENT '碳水化合物总克数',
    `protein_grams`           DECIMAL(10, 2) NULL COMMENT '蛋白质总克数',
    `fat_grams`               DECIMAL(10, 2) NULL COMMENT '脂肪总克数',
    `nutrition_summary`       TEXT           NULL COMMENT '营养文字分析总结',
    `health_suggestions`      TEXT           NULL COMMENT '宏观健康建议',
    `improvement_suggestions` TEXT           NULL COMMENT '具体饮食改进建议',
    `created_at`              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
    `updated_at`              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '报告更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id` ASC),
    INDEX `idx_analysis_date` (`analysis_date` ASC),
    UNIQUE INDEX `uk_user_date` (`user_id` ASC, `analysis_date` ASC),
    CONSTRAINT `fk_daily_diet_analysis_users`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '用户的每日饮食汇总分析报告';


-- -----------------------------------------------------
-- 表 5: `record_likes` - 饮食记录点赞表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `record_likes`;
CREATE TABLE IF NOT EXISTS `record_likes`
(
    `id`         BIGINT    NOT NULL AUTO_INCREMENT COMMENT '点赞记录唯一ID',
    `record_id`  BIGINT    NOT NULL COMMENT '被点赞的饮食记录ID',
    `user_id`    BIGINT    NOT NULL COMMENT '点赞的用户ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id` ASC),
    INDEX `idx_user_id` (`user_id` ASC),
    UNIQUE INDEX `uk_user_record` (`user_id` ASC, `record_id` ASC) COMMENT '联合唯一索引，防止重复点赞',
    CONSTRAINT `fk_record_likes_meal_records`
        FOREIGN KEY (`record_id`)
            REFERENCES `meal_records` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_record_likes_users`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '饮食记录点赞表';


-- -----------------------------------------------------
-- 表 6: `record_comments` - 饮食记录评论表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `record_comments`;
CREATE TABLE IF NOT EXISTS `record_comments`
(
    `id`                BIGINT    NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
    `record_id`         BIGINT    NOT NULL COMMENT '被评论的饮食记录ID',
    `user_id`           BIGINT    NOT NULL COMMENT '评论的用户ID',
    `content`           TEXT      NOT NULL COMMENT '评论内容',
    `parent_comment_id` BIGINT    NULL     DEFAULT NULL COMMENT '回复的父评论ID，NULL为一级评论',
    `created_at`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `updated_at`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id` ASC),
    INDEX `idx_user_id` (`user_id` ASC),
    INDEX `idx_parent_comment_id` (`parent_comment_id` ASC),
    CONSTRAINT `fk_record_comments_meal_records`
        FOREIGN KEY (`record_id`)
            REFERENCES `meal_records` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_record_comments_users`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_record_comments_parent_comment`
        FOREIGN KEY (`parent_comment_id`)
            REFERENCES `record_comments` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '饮食记录评论表（支持楼中楼）';


-- -----------------------------------------------------
-- 表 7: `notifications` - 通用用户通知表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE IF NOT EXISTS `notifications`
(
    `id`                BIGINT                                              NOT NULL AUTO_INCREMENT COMMENT '通知唯一ID',
    `user_id`           BIGINT                                              NOT NULL COMMENT '接收通知的用户ID',
    `sender_user_id`    BIGINT                                              NOT NULL COMMENT '触发通知的用户ID (系统消息时为系统用户ID)',
    `notification_type` ENUM ('meal_reminder', 'system', 'like', 'comment') NOT NULL COMMENT '通知类型',
    `target_record_id`  BIGINT                                              NULL     DEFAULT NULL COMMENT '关联的饮食记录ID, 用于跳转',
    `content`           VARCHAR(255)                                        NULL     DEFAULT NULL COMMENT '通知内容摘要',
    `is_read`           TINYINT(1)                                          NOT NULL DEFAULT 0 COMMENT '是否已读 (0:未读, 1:已读)',
    `created_at`        TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_is_read` (`user_id` ASC, `is_read` ASC),
    INDEX `idx_sender_user_id` (`sender_user_id` ASC),
    INDEX `idx_target_record_id` (`target_record_id` ASC),
    CONSTRAINT `fk_notifications_receiver_users`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_notifications_sender_users`
        FOREIGN KEY (`sender_user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_notifications_meal_records`
        FOREIGN KEY (`target_record_id`)
            REFERENCES `meal_records` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
) ENGINE = InnoDB COMMENT = '通用用户通知表';


-- -----------------------------------------------------
-- 脚本结束
-- -----------------------------------------------------

-- -----------------------------------------------------
-- NutriTrack 数据库初始数据（Mock Data）脚本
-- -----------------------------------------------------

-- 选择要操作的数据库
USE `NutriTrack`;

-- 清空现有数据（可选，在测试环境中重置数据时使用）
-- 注意：SET FOREIGN_KEY_CHECKS 是为了避免因外键约束导致删除失败，操作完后需要恢复
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `notifications`;
TRUNCATE TABLE `record_comments`;
TRUNCATE TABLE `record_likes`;
TRUNCATE TABLE `daily_diet_analysis`;
TRUNCATE TABLE `meal_images`;
TRUNCATE TABLE `meal_records`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;


-- -----------------------------------------------------
-- 1. 插入用户数据
-- 密码 'password123' 对应的 Bcrypt 哈希值为: '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q'
-- -----------------------------------------------------

-- 插入系统及普通用户
INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone_number`, `nickname`, `avatar_url`, `status`)
VALUES (1, 'system', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '系统通知',
        'https://i.pravatar.cc/150?u=system', 'active'),
       (2, 'reminder', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '用餐提醒',
        'https://i.pravatar.cc/150?u=reminder', 'active'),
       (3, 'suggestion', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', NULL, NULL, '健康建议',
        'https://i.pravatar.cc/150?u=suggestion', 'active'),
       (4, 'xwj', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'xwj@example.com', '13800138001',
        '小王几', 'https://i.pravatar.cc/150?u=xwj', 'active'),
       (5, 'pj', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'pj@example.com', '13800138002',
        '皮皮酱', 'https://i.pravatar.cc/150?u=pj', 'active'),
       (6, 'yht', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'yht@example.com', '13800138003',
        '樱花糖', 'https://i.pravatar.cc/150?u=yht', 'active'),
       (7, 'yzq', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'yzq@example.com', '13800138004',
        '月之泣', 'https://i.pravatar.cc/150?u=yzq', 'active'),
       (8, 'ysj', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'ysj@example.com', '13800138005',
        '艺术家', 'https://i.pravatar.cc/150?u=ysj', 'active'),
       (9, 'hjp', '$2a$10$e.ExV2pYml514.e1XAn.2uOg8sLnsd/EMmTEUv362sS3d22Pyi7.q', 'hjp@example.com', '13800138006',
        '黑金派', 'https://i.pravatar.cc/150?u=hjp', 'active');


-- -----------------------------------------------------
-- 2. 插入饮食记录
-- -----------------------------------------------------
INSERT INTO `meal_records` (`id`, `user_id`, `meal_type`, `description`, `calories`, `rating`, `record_date`,
                            `is_shared`)
VALUES (1, 4, 'breakfast', '一杯燕麦拿铁，两片全麦面包，一个煎蛋。开启元气满满的一天！', 350, 4.5, '2025-09-01', 1),
       (2, 4, 'lunch', '公司楼下的轻食沙拉，主要是鸡胸肉和蔬菜。', 450, 4.0, '2025-09-01', 0),
       (3, 4, 'dinner', '晚上和朋友聚餐，吃了寿喜锅，有点放纵了~', 950, 5.0, '2025-09-01', 1),
       (4, 5, 'dinner', '自己做的番茄意面，简单又健康。', 550, 4.5, '2025-09-01', 1),
       (5, 4, 'breakfast', '今天起晚了，只喝了一杯黑咖啡。', 20, 3.0, '2025-09-02', 0),
       (6, 4, 'lunch', '点的外卖麻辣香锅，好吃是好吃，但感觉热量爆炸。', 880, 4.0, '2025-09-02', 1),
       (7, 5, 'breakfast', '自制牛油果奶昔，好喝！', 320, 5.0, '2025-09-02', 1),
       (8, 6, 'lunch', '健身后的午餐，三文鱼、西兰花和糙米饭，非常标准的健身餐。', 580, 5.0, '2025-09-02', 1);


-- -----------------------------------------------------
-- 3. 插入记录图片
-- -----------------------------------------------------
INSERT INTO `meal_images` (`record_id`, `image_url`, `upload_order`)
VALUES (3, 'https://images.unsplash.com/photo-1583894503515-afd8a8451989?q=80&w=1974', 0),
       (4, 'https://images.unsplash.com/photo-1621996346565-e326b20f545c?q=80&w=2080', 0),
       (6, 'https://images.unsplash.com/photo-1625944022823-384343110275?q=80&w=1964', 0),
       (8, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070', 0);


-- -----------------------------------------------------
-- 4. 插入每日饮食分析
-- -----------------------------------------------------
INSERT INTO `daily_diet_analysis` (`user_id`, `analysis_date`, `health_score`, `carb_grams`, `protein_grams`,
                                   `fat_grams`, `nutrition_summary`, `health_suggestions`, `improvement_suggestions`)
VALUES (4, '2025-09-01', 75.50, 180.5, 95.2, 70.8,
        '您今天的早餐和午餐营养均衡，但晚餐的热量和脂肪摄入较高，导致全天总热量略超标。蛋白质摄入充足，值得表扬。',
        '建议在享受聚餐的同时，注意选择清淡一些的锅底和配菜，可以适当减少高脂肪肉类的摄入。保持日常的运动习惯，有助于热量消耗。',
        '晚餐时可以尝试用蔬菜或菌菇类代替部分肉类，增加饱腹感的同时降低脂肪摄入。如果感觉饥饿，可以选择一些低糖水果作为餐后补充。');


-- -----------------------------------------------------
-- 5. 插入点赞记录
-- -----------------------------------------------------
INSERT INTO `record_likes` (`record_id`, `user_id`)
VALUES (3, 5), -- pj 赞了 xwj 的寿喜锅
       (3, 7), -- yzq 赞了 xwj 的寿喜锅
       (4, 4), -- xwj 赞了 pj 的意面
       (8, 4), -- xwj 赞了 yht 的健身餐
       (8, 5);
-- pj 赞了 yht 的健身餐


-- -----------------------------------------------------
-- 6. 插入评论记录 (包含楼中楼回复)
-- -----------------------------------------------------
INSERT INTO `record_comments` (`id`, `record_id`, `user_id`, `content`, `parent_comment_id`)
VALUES (1, 3, 5, '看起来也太好吃了吧！哪家店呀？', NULL),
       (2, 3, 4, '就在公司附近新开的那家哦，下次一起去！', 1), -- xwj 回复 pj
       (3, 8, 9, '太自律了！求一份详细的食谱参考下。', NULL),
       (4, 8, 6, '没问题，我晚点整理一下发给你~', 3);
-- yht 回复 hjp


-- -----------------------------------------------------
-- 7. 插入通知
-- -----------------------------------------------------
INSERT INTO `notifications` (`user_id`, `sender_user_id`, `notification_type`, `target_record_id`, `content`, `is_read`)
VALUES
-- 互动通知
(4, 5, 'like', 3, '皮皮酱 赞了你的记录', 0),
(4, 7, 'like', 3, '月之泣 赞了你的记录', 1),
(5, 4, 'like', 4, '小王几 赞了你的记录', 0),
(6, 4, 'like', 8, '小王几 赞了你的记录', 0),
(6, 5, 'like', 8, '皮皮酱 赞了你的记录', 0),
(4, 5, 'comment', 3, '皮皮酱 评论了你：看起来也太好吃了吧！哪家店呀？', 0),
(6, 9, 'comment', 8, '黑金派 评论了你：太自律了！求一份详细的食谱参考下。', 0),
-- 系统通知
(8, 1, 'system', NULL, '欢迎加入NutriTrack！开始记录你的第一餐，追踪健康生活吧！', 0),
(4, 2, 'meal_reminder', NULL, '晚餐时间到啦，别忘了记录今天吃了什么哦~', 0);


-- -----------------------------------------------------
-- 脚本结束
-- -----------------------------------------------------