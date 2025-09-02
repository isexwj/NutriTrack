/*
 Navicat Premium Dump SQL

 Source Server         : tiger
 Source Server Type    : MySQL
 Source Server Version : 50731 (5.7.31-log)
 Source Host           : localhost:3306
 Source Schema         : template

 Target Server Type    : MySQL
 Target Server Version : 50731 (5.7.31-log)
 File Encoding         : 65001

 Date: 01/09/2025 20:32:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（加密后）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'user' COMMENT '角色（如：admin/user）',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'xwj', '$2a$10$dj2XifEERHHgjmyEx.8kf.ff0IdBfZ5XqqgITC32EjP6te4On95Na', NULL, 'user', 0, NULL, NULL);
INSERT INTO `user` VALUES (4, 'xwjj', 'e10adc3949ba59abbe56e057f20f883e', NULL, 'user', 0, NULL, NULL);
INSERT INTO `user` VALUES (5, 'asuan', '$2a$10$uLYxJN70AZhfG4/lixI/LuoIJnbiji1HTSuVC/j4J67n7j.p8FFlW', NULL, 'USER', 0, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
