package com.petcare.security.service;

/**
 * JWT Token 黑名单服务接口。
 *
 * <p>用于主动撤销尚未过期的 Token。
 * 典型场景：用户登出、修改密码、管理员踢出用户等。</p>
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单。
     *
     * @param jti            Token 的唯一标识符（JWT ID）
     * @param expirationMillis Token 的过期时间（毫秒时间戳），黑名单 TTL 对齐到此时间
     */
    void blacklist(String jti, long expirationMillis);

    /**
     * 检查 Token 是否在黑名单中。
     *
     * @param jti Token 的唯一标识符
     * @return true 表示该 Token 已被撤销
     */
    boolean isBlacklisted(String jti);
}
