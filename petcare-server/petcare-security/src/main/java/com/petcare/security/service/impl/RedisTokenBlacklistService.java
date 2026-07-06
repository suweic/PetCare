package com.petcare.security.service.impl;

import com.petcare.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 JWT Token 黑名单实现。
 *
 * <p>原理：用户登出时将 Token 的 jti 存入 Redis，TTL 与 Token 剩余有效期对齐。
 * 验证时先查黑名单，命中则拒绝。Token 过期后 Redis 自动清除记录，不产生残留数据。</p>
 *
 * <p>性能影响：每次请求增加一次 Redis GET 查询（O(1)）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklist(String jti, long expirationMillis) {
        long now = System.currentTimeMillis();
        long ttlMillis = expirationMillis - now;

        if (ttlMillis <= 0) {
            // Token 已过期，无需加入黑名单
            log.debug("Token 已过期，跳过黑名单: jti={}", jti);
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        stringRedisTemplate.opsForValue().set(key, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
        log.info("Token 已加入黑名单: jti={}, ttlSeconds={}", jti, ttlMillis / 1000);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        String key = BLACKLIST_KEY_PREFIX + jti;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
