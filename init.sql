-- =============================================
-- PetCare 宠物在线问诊系统数据库初始化脚本
-- 数据库版本: MySQL 8.0
-- 创建时间: 2026-06-26
-- 更新日期: 2026-07-02 — 安全加固（生产部署版本）
-- =============================================
-- ⚠️ 重要说明：
--   1. 此脚本使用 CREATE TABLE IF NOT EXISTS，可安全重复执行
--   2. 种子数据使用 INSERT IGNORE，不会覆盖已有数据
--   3. 首次部署执行此脚本即可，后续数据迁移请使用 Flyway/Liquibase
--   4. Docker MySQL entrypoint 仅在首次初始化时执行此脚本
--   5. 如需重置数据库：手动删库后重建容器（docker compose down -v && docker compose up -d）
-- =============================================
-- 🔴 生产环境安全警告（必读！）：
--   此脚本包含仅供开发环境使用的种子数据：
--   (a) 所有测试账号密码均为 "admin123"（bcrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnu）
--   (b) 如果你在生产环境部署，请执行以下操作之一：
--       【推荐】删除下面 "-- BEGIN SEED DATA --" 到 "-- END SEED DATA --" 之间的所有 INSERT 语句
--       【或】部署后立即登录后台修改所有账号密码
--       【或】手动替换下方所有 bcrypt 哈希值为你自己生成的密码哈希
--   (c) 生成新 bcrypt 哈希: 使用在线工具或 bcrypt-cli，确保与 Spring Security 配置一致
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `petcare` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `petcare`;

-- =============================================
-- 1. 用户表 (user)
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(255) DEFAULT NULL COMMENT '身份证号（加密存储）',
  `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户 2-医生 3-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 科室表 (department)
-- =============================================
CREATE TABLE IF NOT EXISTS `department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` VARCHAR(50) NOT NULL COMMENT '科室名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '科室描述',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '科室图标URL',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- =============================================
-- 3. 医生表 (doctor)
-- =============================================
CREATE TABLE IF NOT EXISTS `doctor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `department_id` BIGINT DEFAULT NULL COMMENT '科室ID',
  `title` VARCHAR(50) DEFAULT NULL COMMENT '职称（主治医师、主任医师等）',
  `specialty` VARCHAR(255) DEFAULT NULL COMMENT '专长领域',
  `experience` INT DEFAULT NULL COMMENT '从业年限',
  `education` VARCHAR(100) DEFAULT NULL COMMENT '学历',
  `hospital` VARCHAR(100) DEFAULT NULL COMMENT '所属医院',
  `qualification_cert` VARCHAR(255) DEFAULT NULL COMMENT '资格证URL',
  `practice_cert` VARCHAR(255) DEFAULT NULL COMMENT '执业证URL',
  `introduction` TEXT COMMENT '个人简介',
  `consultation_fee` DECIMAL(8,2) DEFAULT NULL COMMENT '问诊费用',
  `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
  `consultation_count` INT DEFAULT 0 COMMENT '问诊次数',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-正常 2-禁用 3-已拒绝',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_status` (`status`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- =============================================
-- 4. 宠物表 (pet)
-- =============================================
CREATE TABLE IF NOT EXISTS `pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `species` TINYINT NOT NULL COMMENT '物种：1-猫 2-狗 3-其他',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
  `gender` TINYINT DEFAULT NULL COMMENT '性别：1-公 2-母',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重（公斤）',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `medical_history` TEXT COMMENT '病史记录',
  `allergy_info` TEXT COMMENT '过敏信息',
  `sterilized` TINYINT DEFAULT 0 COMMENT '是否绝育：0-否 1-是',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_species` (`species`),
  CONSTRAINT `fk_pet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- =============================================
-- 5. 问诊表 (consultation)
-- =============================================
CREATE TABLE IF NOT EXISTS `consultation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问诊ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '医生ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `department_id` BIGINT DEFAULT NULL COMMENT '科室ID',
  `type` TINYINT NOT NULL COMMENT '问诊类型：1-图文 2-视频 3-语音',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待接单 1-进行中 2-已完成 3-已取消 4-已拒绝 5-超时',
  `chief_complaint` TEXT COMMENT '主诉',
  `symptoms` TEXT COMMENT '症状描述',
  `prescription_id` BIGINT DEFAULT NULL COMMENT '处方ID',
  `scheduled_time` DATETIME DEFAULT NULL COMMENT '预约时间',
  `start_time` DATETIME DEFAULT NULL COMMENT '问诊开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '问诊结束时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  UNIQUE KEY `uk_prescription_id` (`prescription_id`),
  CONSTRAINT `fk_consultation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_consultation_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_consultation_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊表';

-- =============================================
-- 6. 问诊消息表 (consultation_message)
-- =============================================
CREATE TABLE IF NOT EXISTS `consultation_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `consultation_id` BIGINT NOT NULL COMMENT '问诊ID',
  `sender_type` TINYINT NOT NULL COMMENT '发送者类型：1-用户 2-医生',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（sender_type=1时）',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '医生ID（sender_type=2时）',
  `message_type` TINYINT NOT NULL COMMENT '消息类型：1-文本 2-图片 3-语音 4-视频',
  `content` TEXT COMMENT '消息内容',
  `media_url` VARCHAR(255) DEFAULT NULL COMMENT '媒体文件URL',
  `duration` INT DEFAULT NULL COMMENT '媒体时长（秒）',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_consultation_id` (`consultation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_message_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊消息表';

-- =============================================
-- 7. 问诊图片表 (consultation_image)
-- =============================================
CREATE TABLE IF NOT EXISTS `consultation_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `consultation_id` BIGINT NOT NULL COMMENT '问诊ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_consultation_id` (`consultation_id`),
  CONSTRAINT `fk_image_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊图片表';

-- =============================================
-- 8. 处方表 (prescription)
-- =============================================
CREATE TABLE IF NOT EXISTS `prescription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '处方ID',
  `consultation_id` BIGINT NOT NULL COMMENT '问诊ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `diagnosis` TEXT COMMENT '诊断结果',
  `advice` TEXT COMMENT '用药建议',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-已开具 2-已审核 3-已拒绝',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_id` (`consultation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_prescription_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_prescription_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_prescription_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_prescription_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- =============================================
-- 9. 药品字典表 (medicine)
-- =============================================
CREATE TABLE IF NOT EXISTS `medicine` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '药品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '药品名称',
  `generic_name` VARCHAR(100) DEFAULT NULL COMMENT '通用名',
  `specification` VARCHAR(50) DEFAULT NULL COMMENT '规格',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `price` DECIMAL(8,2) DEFAULT NULL COMMENT '价格',
  `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '药品类别',
  `description` TEXT COMMENT '药品说明',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_generic_name` (`generic_name`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品字典表';

-- =============================================
-- 10. 处方明细表 (prescription_item)
-- =============================================
CREATE TABLE IF NOT EXISTS `prescription_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
  `medicine_id` BIGINT DEFAULT NULL COMMENT '药品ID',
  `medicine_name` VARCHAR(100) NOT NULL COMMENT '药品名称（冗余，方便查看）',
  `specification` VARCHAR(50) DEFAULT NULL COMMENT '规格',
  `dosage` VARCHAR(50) NOT NULL COMMENT '用量',
  `frequency` VARCHAR(50) NOT NULL COMMENT '频次',
  `duration` VARCHAR(50) NOT NULL COMMENT '用药时长',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_prescription_id` (`prescription_id`),
  KEY `idx_medicine_id` (`medicine_id`),
  CONSTRAINT `fk_item_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- =============================================
-- 11. 订单表 (order)
-- =============================================
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `consultation_id` BIGINT DEFAULT NULL COMMENT '问诊ID',
  `type` TINYINT NOT NULL COMMENT '订单类型：1-问诊 2-药品 3-会员',
  `amount` DECIMAL(8,2) NOT NULL COMMENT '订单金额',
  `discount_amount` DECIMAL(8,2) DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` DECIMAL(8,2) NOT NULL COMMENT '实付金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已取消 3-已退款 4-已关闭',
  `payment_method` TINYINT DEFAULT NULL COMMENT '支付方式：1-微信 2-支付宝 3-余额',
  `payment_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付流水号',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_consultation_id` (`consultation_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================
-- 12. 评价表 (evaluation)
-- =============================================
CREATE TABLE IF NOT EXISTS `evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `consultation_id` BIGINT NOT NULL COMMENT '问诊ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `rating` TINYINT NOT NULL COMMENT '评分1-5',
  `content` TEXT COMMENT '评价内容',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
  `reply` TEXT COMMENT '医生回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-显示',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_id` (`consultation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_is_anonymous` (`is_anonymous`),
  CONSTRAINT `fk_evaluation_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_evaluation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_evaluation_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =============================================
-- 13. 评价标签表 (evaluation_tag)
-- =============================================
CREATE TABLE IF NOT EXISTS `evaluation_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `evaluation_id` BIGINT NOT NULL COMMENT '评价ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_evaluation_id` (`evaluation_id`),
  KEY `idx_tag_name` (`tag_name`),
  CONSTRAINT `fk_tag_evaluation` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签表';

-- =============================================
-- 14. 医生排班表 (doctor_schedule)
-- =============================================
CREATE TABLE IF NOT EXISTS `doctor_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `schedule_date` DATE NOT NULL COMMENT '排班日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-休息 1-出诊 2-已约满',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_date_time` (`doctor_id`, `schedule_date`, `start_time`, `end_time`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- =============================================
-- 15. 管理员表 (admin)
-- =============================================
CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================
-- 16. 角色表 (role)
-- =============================================
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 17. 角色权限关联表 (role_menu)
-- =============================================
CREATE TABLE IF NOT EXISTS `role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 18. 菜单表 (menu)
-- =============================================
CREATE TABLE IF NOT EXISTS `menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `menu_code` VARCHAR(100) DEFAULT NULL COMMENT '菜单编码',
  `path` VARCHAR(100) DEFAULT NULL COMMENT '路由路径',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_code` (`menu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- =============================================
-- 19. 操作日志表 (operation_log)
-- =============================================
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块名称',
  `operation` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT COMMENT '请求参数',
  `response_result` TEXT COMMENT '响应结果',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `execution_time` INT DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =============================================
-- 20. 疫苗记录表 (vaccine_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `vaccine_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '疫苗记录ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `vaccine_name` VARCHAR(100) NOT NULL COMMENT '疫苗名称',
  `vaccination_date` DATE NOT NULL COMMENT '接种日期',
  `next_vaccination_date` DATE DEFAULT NULL COMMENT '下次接种日期',
  `hospital` VARCHAR(100) DEFAULT NULL COMMENT '接种医院',
  `remarks` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_vaccination_date` (`vaccination_date`),
  CONSTRAINT `fk_vaccine_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗记录表';

-- =============================================
-- 21. 系统配置表 (system_config)
-- =============================================
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
  `group_name` VARCHAR(50) DEFAULT NULL COMMENT '配置分组',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 22. 通知表 (notification)
-- =============================================
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '通知类型：1-问诊提醒 2-处方通知 3-订单通知 4-系统公告',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- =============================================
-- 23. 收货地址表 (user_address)
-- =============================================
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(11) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- =============================================
-- 24. 医生审核记录表 (doctor_audit_log)
-- =============================================
CREATE TABLE IF NOT EXISTS `doctor_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_status` TINYINT NOT NULL COMMENT '审核状态：1-通过 2-拒绝',
  `audit_comment` VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_audit_user_id` (`audit_user_id`),
  CONSTRAINT `fk_audit_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生审核记录表';

-- =============================================
-- 初始化数据（⚠️ 仅用于开发环境）
-- =============================================
-- BEGIN SEED DATA --  生产环境请删除此标记之间的所有 INSERT 语句
-- =============================================

-- 初始化角色（admin表依赖role表）
INSERT IGNORE INTO `role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, NOW()),
(2, '运营管理员', 'OPERATION_ADMIN', '负责用户、订单、内容管理', 1, NOW()),
(3, '审核员', 'AUDITOR', '负责医生资质审核', 1, NOW());

-- 初始化管理员（1条）
INSERT IGNORE INTO `admin` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`, `create_time`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '超级管理员', '13800138000', 'admin@petcare.com', 1, 1, NOW());

-- 初始化科室（6个）
INSERT IGNORE INTO `department` (`id`, `name`, `description`, `icon`, `sort`, `status`, `create_time`) VALUES
(1, '内科', '负责宠物常见内科疾病的诊断和治疗', 'internal-medicine.png', 1, 1, NOW()),
(2, '外科', '负责宠物外科手术和外伤处理', 'surgery.png', 2, 1, NOW()),
(3, '皮肤科', '负责宠物皮肤病的诊断和治疗', 'dermatology.png', 3, 1, NOW()),
(4, '眼科', '负责宠物眼部疾病的诊断和治疗', 'ophthalmology.png', 4, 1, NOW()),
(5, '口腔科', '负责宠物口腔健康和牙齿护理', 'dentistry.png', 5, 1, NOW()),
(6, '营养科', '负责宠物营养咨询和饮食指导', 'nutrition.png', 6, 1, NOW());

-- 初始化医生用户（3条，doctor表依赖user表）
INSERT IGNORE INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `real_name`, `user_type`, `status`, `create_time`) VALUES
(1001, '13900139001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '李医生', 'https://petcare.com/avatar/doctor1.png', '李明', 2, 1, NOW()),
(1002, '13900139002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '王医生', 'https://petcare.com/avatar/doctor2.png', '王芳', 2, 1, NOW()),
(1003, '13900139003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '张医生', 'https://petcare.com/avatar/doctor3.png', '张伟', 2, 1, NOW());

-- 初始化医生（3条测试数据）
INSERT IGNORE INTO `doctor` (`id`, `user_id`, `department_id`, `title`, `specialty`, `experience`, `education`, `hospital`, `introduction`, `consultation_fee`, `rating`, `consultation_count`, `status`, `create_time`) VALUES
(1, 1001, 1, '主治医师', '犬猫内科疾病、消化系统疾病、呼吸系统疾病', 10, '硕士', '北京动物医院', '10年宠物内科临床经验，擅长犬猫消化系统疾病、呼吸系统疾病、内分泌疾病的诊断与治疗。', 68.00, 4.8, 156, 1, NOW()),
(2, 1002, 2, '副主任医师', '犬猫外科手术、骨折修复、肿瘤切除', 15, '博士', '上海宠物专科医院', '15年宠物外科临床经验，擅长各种外科手术，包括骨折修复、肿瘤切除、软组织手术等。', 88.00, 4.9, 234, 1, NOW()),
(3, 1003, 3, '主治医师', '犬猫皮肤病、过敏性皮炎、真菌性皮肤病', 8, '硕士', '广州宠物诊所', '8年宠物皮肤科临床经验，擅长犬猫过敏性皮炎、真菌性皮肤病、寄生虫性皮炎的诊断与治疗。', 58.00, 4.7, 98, 1, NOW());

-- 初始化普通用户测试数据
INSERT IGNORE INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `real_name`, `user_type`, `status`, `create_time`) VALUES
(1, '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '铲屎官小王', 'https://petcare.com/avatar/user1.png', '王小华', 1, 1, NOW()),
(2, '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '猫奴小李', 'https://petcare.com/avatar/user2.png', '李小红', 1, 1, NOW()),
(3, '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '爱宠达人', 'https://petcare.com/avatar/user3.png', '张小明', 1, 1, NOW());

-- 初始化宠物测试数据
INSERT IGNORE INTO `pet` (`id`, `user_id`, `name`, `species`, `breed`, `gender`, `birthday`, `weight`, `avatar`, `medical_history`, `allergy_info`, `sterilized`, `create_time`) VALUES
(1, 1, '旺财', 2, '金毛', 1, '2022-06-01', 25.50, 'https://petcare.com/avatar/pet1.png', '无', '无', 1, NOW()),
(2, 1, '咪咪', 1, '英短', 2, '2023-03-15', 4.20, 'https://petcare.com/avatar/pet2.png', '曾患猫癣', '无', 0, NOW()),
(3, 2, '雪球', 1, '布偶', 1, '2023-08-20', 5.80, 'https://petcare.com/avatar/pet3.png', '无', '对鸡肉过敏', 1, NOW());

-- 初始化问诊测试数据
INSERT IGNORE INTO `consultation` (`id`, `user_id`, `doctor_id`, `pet_id`, `department_id`, `type`, `status`, `chief_complaint`, `symptoms`, `scheduled_time`, `start_time`, `end_time`, `create_time`) VALUES
(1, 1, 1, 1, 1, 1, 2, '食欲不振', '最近3天食欲明显下降，精神萎靡，偶尔呕吐白色泡沫', '2026-06-20 10:00:00', '2026-06-20 10:00:00', '2026-06-20 10:30:00', '2026-06-20 09:30:00'),
(2, 2, 3, 3, 3, 1, 1, '皮肤瘙痒', '全身瘙痒，频繁抓挠，背部毛发脱落', '2026-06-26 14:00:00', '2026-06-26 14:00:00', NULL, '2026-06-26 13:30:00'),
(3, 3, NULL, 1, 2, 1, 0, '后腿跛行', '昨天出门玩耍后回来发现后腿不敢着地，走路跛行', NULL, NULL, NULL, NOW());

-- 初始化问诊消息测试数据
INSERT IGNORE INTO `consultation_message` (`id`, `consultation_id`, `sender_type`, `sender_id`, `user_id`, `doctor_id`, `message_type`, `content`, `is_read`, `create_time`) VALUES
(1, 1, 1, 1, 1, NULL, 1, '医生您好，我家狗狗最近食欲不振，精神也不太好', 1, '2026-06-20 10:01:00'),
(2, 1, 2, 1, NULL, 1, 1, '您好，请问狗狗最近饮食有变化吗？有没有出现呕吐或腹泻？', 1, '2026-06-20 10:02:00'),
(3, 1, 1, 1, 1, NULL, 1, '饮食和平时一样，昨天吐了两次白色泡沫', 1, '2026-06-20 10:03:00'),
(4, 1, 2, 1, NULL, 1, 1, '根据您描述的情况，可能是肠胃问题，建议禁食观察，若持续呕吐请及时就医', 1, '2026-06-20 10:25:00'),
(5, 2, 1, 2, 2, NULL, 1, '医生您好，我家猫咪一直抓挠身体，背部毛发都掉了一块', 1, '2026-06-26 14:01:00'),
(6, 2, 2, 3, NULL, 3, 1, '您好，请拍一张患处的照片发过来，我需要看一下具体情况', 0, '2026-06-26 14:02:00');

-- 初始化处方测试数据
INSERT IGNORE INTO `prescription` (`id`, `consultation_id`, `user_id`, `doctor_id`, `pet_id`, `diagnosis`, `advice`, `status`, `create_time`) VALUES
(1, 1, 1, 1, 1, '犬急性胃炎', '1. 禁食24小时，可少量饮水\r\n2. 口服奥美拉唑，每日1次，每次1粒\r\n3. 口服益生菌调理肠胃\r\n4. 若持续呕吐或出现其他异常，及时就医', 2, '2026-06-20 10:30:00');

-- 初始化药品字典测试数据
INSERT IGNORE INTO `medicine` (`id`, `name`, `generic_name`, `specification`, `unit`, `price`, `manufacturer`, `category`, `description`, `status`, `create_time`) VALUES
(1, '奥美拉唑胶囊', '奥美拉唑', '20mg*14粒', '盒', 35.00, '阿斯利康', '消化系统', '用于治疗胃溃疡、十二指肠溃疡等', 1, NOW()),
(2, '益生菌粉', '复合益生菌', '2g*30袋', '盒', 68.00, '汤臣倍健', '调节肠道', '调节肠道菌群平衡', 1, NOW()),
(3, '阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '盒', 25.00, '哈药集团', '抗生素', '用于敏感菌引起的感染', 1, NOW()),
(4, '伊曲康唑胶囊', '伊曲康唑', '0.1g*14粒', '盒', 85.00, '辉瑞', '抗真菌', '用于治疗真菌感染', 1, NOW()),
(5, '氯雷他定片', '氯雷他定', '10mg*12片', '盒', 28.00, '开瑞坦', '抗过敏', '用于缓解过敏性皮炎症状', 1, NOW());

-- 初始化处方明细测试数据
INSERT IGNORE INTO `prescription_item` (`id`, `prescription_id`, `medicine_id`, `medicine_name`, `specification`, `dosage`, `frequency`, `duration`, `quantity`, `create_time`) VALUES
(1, 1, 1, '奥美拉唑胶囊', '20mg*14粒', '1粒/次', '每日1次', '3天', 1, '2026-06-20 10:30:00'),
(2, 1, 2, '益生菌粉', '2g*30袋', '1袋/次', '每日2次', '5天', 1, '2026-06-20 10:30:00');

-- 初始化订单测试数据
INSERT IGNORE INTO `order` (`id`, `order_no`, `user_id`, `consultation_id`, `type`, `amount`, `discount_amount`, `actual_amount`, `status`, `payment_method`, `payment_no`, `payment_time`, `create_time`) VALUES
(1, 'PC2026062000001', 1, 1, 1, 68.00, 0.00, 68.00, 1, 1, 'WX2026062010001', '2026-06-20 09:45:00', '2026-06-20 09:30:00'),
(2, 'PC2026062600002', 2, 2, 1, 58.00, 0.00, 58.00, 1, 1, 'WX2026062614001', '2026-06-26 13:45:00', '2026-06-26 13:30:00'),
(3, 'PC2026062600003', 3, 3, 1, 88.00, 0.00, 88.00, 0, NULL, NULL, NULL, NOW());

-- 初始化评价测试数据
INSERT IGNORE INTO `evaluation` (`id`, `consultation_id`, `user_id`, `doctor_id`, `rating`, `content`, `is_anonymous`, `reply`, `reply_time`, `status`, `create_time`) VALUES
(1, 1, 1, 1, 5, '李医生非常专业，诊断准确，给出的建议很实用！狗狗已经恢复健康了，非常感谢！', 0, '很高兴能帮助到您和您的宠物，祝狗狗健康快乐！', '2026-06-21 10:00:00', 1, '2026-06-21 09:00:00');

-- 初始化评价标签测试数据
INSERT IGNORE INTO `evaluation_tag` (`id`, `evaluation_id`, `tag_name`, `create_time`) VALUES
(1, 1, '专业', NOW()),
(2, 1, '耐心', NOW()),
(3, 1, '有效', NOW());

-- 初始化医生排班测试数据
INSERT IGNORE INTO `doctor_schedule` (`id`, `doctor_id`, `schedule_date`, `start_time`, `end_time`, `status`, `create_time`) VALUES
(1, 1, '2026-06-27', '09:00:00', '12:00:00', 1, NOW()),
(2, 1, '2026-06-27', '14:00:00', '18:00:00', 1, NOW()),
(3, 2, '2026-06-27', '10:00:00', '13:00:00', 1, NOW()),
(4, 2, '2026-06-27', '15:00:00', '19:00:00', 1, NOW()),
(5, 3, '2026-06-27', '09:00:00', '17:00:00', 1, NOW());

-- 初始化系统配置测试数据
INSERT IGNORE INTO `system_config` (`id`, `config_key`, `config_value`, `config_desc`, `group_name`, `status`, `create_time`) VALUES
(1, 'system.name', 'PetCare宠物在线问诊', '系统名称', 'system', 1, NOW()),
(2, 'system.logo', '/logo.png', '系统Logo', 'system', 1, NOW()),
(3, 'consultation.timeout', '30', '问诊超时时间（分钟）', 'consultation', 1, NOW()),
(4, 'consultation.cancel.time', '5', '问诊取消时间（分钟）', 'consultation', 1, NOW()),
(5, 'payment.timeout', '15', '支付超时时间（分钟）', 'payment', 1, NOW()),
(6, 'withdrawal.min', '100', '最低提现金额', 'withdrawal', 1, NOW()),
(7, 'withdrawal.fee.rate', '0.01', '提现手续费率', 'withdrawal', 1, NOW()),
(8, 'upload.image.max.size', '5', '图片上传最大大小（MB）', 'upload', 1, NOW()),
(9, 'upload.image.types', 'jpg,jpeg,png,gif', '允许上传的图片类型', 'upload', 1, NOW());

-- =============================================
-- END SEED DATA --  生产环境请删除此标记之间的所有 INSERT 语句
-- =============================================

-- =============================================
-- 数据库初始化完成
-- =============================================
-- 共24张表，含开发环境种子数据
-- 所有种子账号密码均为 bcrypt hash，明文请参考项目文档
-- =============================================