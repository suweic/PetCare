-- =============================================
-- V2: RefreshToken 表
-- 支持 HttpOnly Cookie + Refresh Token 认证模式
-- =============================================
CREATE TABLE IF NOT EXISTS `refresh_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_type` TINYINT NOT NULL COMMENT '用户类型：1-普通用户 2-医生 3-管理员',
  `token` VARCHAR(255) NOT NULL COMMENT 'Refresh Token (UUID)',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `revoked` TINYINT DEFAULT 0 COMMENT '是否已撤销：0-有效 1-已撤销',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RefreshToken表（HttpOnly Cookie认证方案）';
