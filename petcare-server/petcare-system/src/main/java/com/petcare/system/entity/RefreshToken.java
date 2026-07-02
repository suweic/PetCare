package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Refresh Token 实体。
 * <p>
 * 用于实现 HttpOnly Secure SameSite Cookie + Refresh Token 认证模式。
 * Access Token 短期有效（15分钟），Refresh Token 长期有效（7天），
 * 存储在 HttpOnly Cookie 中，JavaScript 无法读取，可有效防御 XSS 攻击下 Token 泄露。
 */
@Getter
@Setter
@TableName("refresh_token")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 用户类型: 1-普通用户 2-医生 3-管理员 */
    private Integer userType;

    /** Refresh Token 值（UUID） */
    private String token;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 是否已撤销 */
    private Boolean revoked;

    /** 创建时间 */
    private LocalDateTime createTime;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isValid() {
        return !Boolean.TRUE.equals(revoked) && !isExpired();
    }
}
