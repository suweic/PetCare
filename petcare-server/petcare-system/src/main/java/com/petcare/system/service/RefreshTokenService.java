package com.petcare.system.service;

import com.petcare.system.entity.RefreshToken;

/**
 * Refresh Token 服务。
 * <p>
 * 用于实现 HttpOnly Secure Cookie 认证方案：
 * <ol>
 *   <li>登录成功后生成 RefreshToken，写入 HttpOnly Cookie</li>
 *   <li>Access Token 过期后，前端自动携带 Cookie 请求刷新</li>
 *   <li>后端验证 RefreshToken → 签发新 Access Token → 轮换 RefreshToken</li>
 * </ol>
 * <p>
 * 安全特性：每次使用后轮换 Token（Token Rotation），
 * 检测到 Token 重放时撤销整个 Token 家族（Reuse Detection）。
 */
public interface RefreshTokenService {

    /**
     * 创建 Refresh Token。
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 创建的 RefreshToken
     */
    RefreshToken create(Long userId, Integer userType);

    /**
     * 验证并轮换 Refresh Token。
     * 返回新的 RefreshToken，旧的标记为已使用。
     *
     * @param token Refresh Token 值
     * @return 新的 RefreshToken，或 null（无效/过期/重放）
     */
    RefreshToken validateAndRotate(String token);

    /**
     * 撤销用户所有 Refresh Token（登出、改密等场景）。
     *
     * @param userId 用户ID
     */
    void revokeAll(Long userId);
}
