package com.petcare.system.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * API 全局限流拦截器。
 * <p>
 * 基于 Redis 实现简单的滑动窗口限流，按 IP 维度限制请求频率。
 * 默认：每个 IP 每秒最多 20 个请求。
 * </p>
 * <p>
 * 限流仅在 Redis 可用时生效；Redis 不可用时放行所有请求（避免级联故障）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_SECONDS = 1;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp;

        try {
            long now = System.currentTimeMillis();
            long windowStart = now - WINDOW_SECONDS * 1000;

            // 滑动窗口：使用 Redis Sorted Set，score 为时间戳
            // 1. 移除窗口外的旧记录
            stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            // 2. 统计当前窗口内的请求数
            Long count = stringRedisTemplate.opsForZSet().zCard(key);
            // 3. 检查是否超限
            if (count != null && count >= MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null,\"timestamp\":"
                                + now + "}");
                log.warn("API限流触发: ip={}, count={}", clientIp, count);
                return false;
            }
            // 4. 记录本次请求（使用纳秒级 member 避免冲突）
            stringRedisTemplate.opsForZSet().add(key,
                    String.valueOf(now) + ":" + Thread.currentThread().getId(),
                    now);
            // 5. 设置过期时间（窗口结束后自动清理整条 key）
            stringRedisTemplate.expire(key, WINDOW_SECONDS + 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Redis 不可用时放行，避免级联故障
            log.debug("限流检查跳过（Redis不可用）: {}", e.getMessage());
        }

        return true;
    }

    /**
     * 获取客户端真实 IP。
     * <p>
     * 注意：如果应用部署在反向代理（Nginx/Ingress）后面，需要在代理层配置
     * 可信代理。当前默认使用 remoteAddr 作为安全底限，避免 X-Forwarded-For 伪造。
     * 生产环境若需支持代理，应将代理 IP 加入可信列表后再读取 X-Forwarded-For。
     * </p>
     */
    private String getClientIp(HttpServletRequest request) {
        // 优先使用 remoteAddr（无法伪造），适用于非代理环境
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        return "0.0.0.0";
    }
}
