-- =============================================
-- PetCare 宠物在线问诊系统数据库表结构
-- 数据库版本: MySQL 8.0
-- 创建时间: 2026-06-26
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `petcare` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `petcare`;

-- =============================================
-- 1. 用户表 (user)
-- =============================================
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(255) DEFAULT NULL COMMENT '身份证号（加密存储）',
  `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户 2-医生',
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
CREATE TABLE `department` (
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
CREATE TABLE `doctor` (
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
CREATE TABLE `pet` (
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
CREATE TABLE `consultation` (
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
CREATE TABLE `consultation_message` (
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
CREATE TABLE `consultation_image` (
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
CREATE TABLE `prescription` (
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
CREATE TABLE `medicine` (
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
CREATE TABLE `prescription_item` (
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
CREATE TABLE `order` (
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
CREATE TABLE `evaluation` (
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
CREATE TABLE `evaluation_tag` (
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
CREATE TABLE `doctor_schedule` (
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
CREATE TABLE `admin` (
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
CREATE TABLE `role` (
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
CREATE TABLE `role_menu` (
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
CREATE TABLE `menu` (
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
CREATE TABLE `operation_log` (
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
CREATE TABLE `vaccine_record` (
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
CREATE TABLE `system_config` (
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
CREATE TABLE `notification` (
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
CREATE TABLE `user_address` (
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
CREATE TABLE `doctor_audit_log` (
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
-- 25. AI预问诊记录表 (pre_consultation)
-- =============================================
CREATE TABLE `pre_consultation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预问诊ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID（可选）',
  `species` TINYINT DEFAULT NULL COMMENT '物种：1-猫 2-狗 3-其他',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
  `age_years` INT DEFAULT NULL COMMENT '年龄（年）',
  `age_months` INT DEFAULT NULL COMMENT '年龄（月）',
  `symptoms` TEXT NOT NULL COMMENT '症状描述',
  `symptom_duration` VARCHAR(50) DEFAULT NULL COMMENT '症状持续时间',
  `additional_info` TEXT DEFAULT NULL COMMENT '补充信息',
  `ai_analysis` TEXT DEFAULT NULL COMMENT 'AI分析结果',
  `recommended_department_id` BIGINT DEFAULT NULL COMMENT 'AI推荐科室ID',
  `recommended_department_name` VARCHAR(50) DEFAULT NULL COMMENT 'AI推荐科室名称',
  `recommended_doctors` JSON DEFAULT NULL COMMENT 'AI推荐医生列表（JSON）',
  `general_advice` TEXT DEFAULT NULL COMMENT 'AI通用建议',
  `llm_model` VARCHAR(100) DEFAULT NULL COMMENT '使用的LLM模型',
  `llm_tokens` INT DEFAULT NULL COMMENT 'LLM消耗token数',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_department_id` (`recommended_department_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_pre_consultation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pre_consultation_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI预问诊记录表';

-- =============================================
-- 初始化数据
-- =============================================

-- ⚠️⚠️⚠️ 安全警告 ⚠️⚠️⚠️
-- 以下 INSERT 语句中使用的 bcrypt 哈希对应明文密码为 "admin123"。
-- 这是仅供开发/测试环境使用的默认密码 —— 生产环境部署前必须更改！
-- 更改方式（二选一）：
--   方案A: 将下方 password 替换为新密码的 bcrypt 哈希
--          生成命令: python -c "import bcrypt; print(bcrypt.hashpw(b'新密码', bcrypt.gensalt()).decode())"
--          或使用在线工具: https://www.bcryptcalculator.com/
--   方案B: 系统启动后立即登录后台，通过修改密码功能变更
--   方案C: 生产环境不执行 init.sql，使用独立的凭据管理方案
-- ⚠️⚠️⚠️ 请务必将此密码更改为强密码 ⚠️⚠️⚠️
INSERT INTO `admin` (`username`, `password`, `real_name`, `phone`, `email`, `role_id`, `status`, `create_time`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHthJEqCxaYFCG3tE.9W', '超级管理员', '13800138000', 'admin@petcare.com', 1, 1, NOW());

-- 初始化默认角色
INSERT INTO `role` (`role_name`, `role_code`, `description`, `status`, `create_time`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, NOW()),
('运营管理员', 'OPERATION_ADMIN', '负责用户、订单、内容管理', 1, NOW()),
('审核员', 'AUDITOR', '负责医生资质审核', 1, NOW());

-- 初始化默认科室
INSERT INTO `department` (`name`, `description`, `icon`, `sort`, `status`, `create_time`) VALUES
('内科', '负责宠物常见内科疾病的诊断和治疗', 'internal-medicine.png', 1, 1, NOW()),
('外科', '负责宠物外科手术和外伤处理', 'surgery.png', 2, 1, NOW()),
('皮肤科', '负责宠物皮肤病的诊断和治疗', 'dermatology.png', 3, 1, NOW()),
('眼科', '负责宠物眼部疾病的诊断和治疗', 'ophthalmology.png', 4, 1, NOW()),
('口腔科', '负责宠物口腔健康和牙齿护理', 'dentistry.png', 5, 1, NOW()),
('营养科', '负责宠物营养咨询和饮食指导', 'nutrition.png', 6, 1, NOW()),
('行为学', '负责宠物行为问题分析和训练指导', 'behavior.png', 7, 1, NOW()),
('急诊科', '24小时急诊服务', 'emergency.png', 8, 1, NOW());

-- 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `group_name`, `status`, `create_time`) VALUES
('system.name', 'PetCare宠物在线问诊', '系统名称', 'system', 1, NOW()),
('system.logo', '/logo.png', '系统Logo', 'system', 1, NOW()),
('consultation.timeout', '30', '问诊超时时间（分钟）', 'consultation', 1, NOW()),
('consultation.cancel.time', '5', '问诊取消时间（分钟）', 'consultation', 1, NOW()),
('payment.timeout', '15', '支付超时时间（分钟）', 'payment', 1, NOW()),
('withdrawal.min', '100', '最低提现金额', 'withdrawal', 1, NOW()),
('withdrawal.fee.rate', '0.01', '提现手续费率', 'withdrawal', 1, NOW()),
('upload.image.max.size', '5', '图片上传最大大小（MB）', 'upload', 1, NOW()),
('upload.image.types', 'jpg,jpeg,png,gif', '允许上传的图片类型', 'upload', 1, NOW());

-- 初始化药品字典示例
INSERT INTO `medicine` (`name`, `generic_name`, `specification`, `unit`, `price`, `manufacturer`, `category`, `description`, `status`, `create_time`) VALUES
('阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '盒', 25.00, '哈药集团制药总厂', '抗生素', '用于敏感菌引起的各种感染', 1, NOW()),
('头孢拉定胶囊', '头孢拉定', '0.25g*24粒', '盒', 35.00, '扬子江药业集团', '抗生素', '用于敏感菌所致的呼吸道感染', 1, NOW()),
('布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '盒', 45.00, '芬必得', '解热镇痛', '用于缓解轻至中度疼痛', 1, NOW()),
('蒙脱石散', '蒙脱石', '3g*10袋', '盒', 15.00, '思密达', '止泻药', '用于成人及儿童急、慢性腹泻', 1, NOW()),
('益生菌粉', '复合益生菌', '2g*30袋', '盒', 68.00, '汤臣倍健', '调节肠道', '调节肠道菌群平衡', 1, NOW());

-- =============================================
-- 数据库设计完成
-- =============================================
-- 共25张表，包括：
-- P0核心表：user, doctor, pet, consultation, consultation_message, prescription, order, evaluation, department, admin, pre_consultation
-- P1重要表：medicine, prescription_item, consultation_image, evaluation_tag
-- P2辅助表：role, role_menu, menu, operation_log, system_config
-- P3扩展表：notification, user_address, doctor_audit_log, vaccine_record
-- =============================================