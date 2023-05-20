/*
 Navicat Premium Data Transfer

 Source Server         : localhost@root
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : sts

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 20/05/2023 20:45:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sts_invt
-- ----------------------------
DROP TABLE IF EXISTS `sts_invt`;
CREATE TABLE `sts_invt`  (
  `invt_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `res_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quantity` decimal(10, 0) NULL DEFAULT NULL,
  PRIMARY KEY (`invt_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sts_invt
-- ----------------------------
INSERT INTO `sts_invt` VALUES ('43907920-baf2-4ec8-b580-b3fd1fc973e2', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 22);

-- ----------------------------
-- Table structure for sts_order
-- ----------------------------
DROP TABLE IF EXISTS `sts_order`;
CREATE TABLE `sts_order`  (
  `order_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `goods_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quantity` decimal(10, 0) NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sts_order
-- ----------------------------
INSERT INTO `sts_order` VALUES ('007de363-d042-460b-a112-33ea43c3712c', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('108f21d5-a393-4666-aece-42f7e87baf29', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('11c5c63f-6dbb-46dc-889e-202f6a040e47', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('1c150217-c536-480e-9085-35df4e471936', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('200a8b54-acde-4167-b4c1-3bf73d9a7115', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('27098959-2238-4869-a63c-f4c79b339e0e', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('2b41b264-bde4-4f14-8693-17e51d6fd780', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('3b52cf26-516c-47b6-bb95-344b0f012486', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('43907920-baf2-4ec8-b580-b3fd1fc973e2', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('486fcbb3-5930-4665-8940-355fc25884ef', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('4cb87a86-814d-4014-8d6b-ff71717f4ebc', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('510b989c-8cad-42d7-9cf7-17e438acd2be', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('51b49465-c592-47a7-acf1-429d9a0c46a4', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('67b55ec2-3202-413a-8a73-38e2de441b96', '1231322', 1);
INSERT INTO `sts_order` VALUES ('87805811-04e2-4fdd-9768-b10e6cfad87e', '1231322', 1);
INSERT INTO `sts_order` VALUES ('888889e3-e0c0-4953-a6d4-abe4c5b932f2', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('8bf76032-e6bc-4f7a-9354-a737388303c8', '1231322', 1);
INSERT INTO `sts_order` VALUES ('8f30a1fb-e759-440f-b00f-551cf7d2def8', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('9133e464-f588-4abc-9d08-ccea1c4288a2', '1231322', 1);
INSERT INTO `sts_order` VALUES ('9ddccab5-b2fb-41ca-81b9-639df3588c94', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('a0a7c7fe-5610-4326-9f35-65d50516c3be', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('a10699c6-ad10-4961-8530-194c0f449e46', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('a755dc8e-7f69-4ae7-b5b6-ecfca8926caf', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('b00be830-c15d-4446-8cfb-60be22009691', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('be8da997-4a66-463d-a8f6-8d120b36df80', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('bfcc916e-057c-470f-b618-6d6486049f12', '1231322', 1);
INSERT INTO `sts_order` VALUES ('c3f1870b-8ac4-48b4-a855-20203908af62', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('c59ebf78-4645-4704-bb37-18daae35ca41', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('cb4df350-874f-4a57-b145-51617ca02271', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('d6684fa0-a8ee-4734-b1c0-06a6b956d704', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('e24c6740-e907-4443-86ba-fb3ffba6f6e9', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('e3591205-2b6d-4cba-9a31-6842b75b23ca', '1231322', 1);
INSERT INTO `sts_order` VALUES ('eb233728-fdeb-4bd2-9f3d-363eb2f357e6', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('ef70ddcd-3c6e-46d1-a6aa-0dcff593186c', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);
INSERT INTO `sts_order` VALUES ('fa718f1f-e3cd-468a-82a5-f94c2c2ceb63', 'fb76ba63-795a-4231-ab8f-f9071dab6363', 1);

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `xid` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `context` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of undo_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
