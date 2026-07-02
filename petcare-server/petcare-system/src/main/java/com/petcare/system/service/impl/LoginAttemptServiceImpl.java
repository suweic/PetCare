package com.petcare.system.service.impl;

import com.petcare.system.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOCK_PREFIX = "login:lock:";

    /** 最大允许失败次数 */
    private static final int MAX_ATTEMPTS = 5;

    /** 失败计数过期时间（分钟） */
    private static final long ATTEMPT_EXPIRE_MINUTES = 30;

    /** 锁定时间（分钟） */
    private static final long LOCK_DURATION_MINUTES = 15;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void recordFailedAttempt(String key) {
        String attemptKey = ATTEMPT_PREFIX + key;
        Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);

        // 首次设置过期时间
        if (attempts != null && attempts == 1) {
            stringRedisTemplate.expire(attemptKey, ATTEMPT_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        // 超过最大尝试次数，锁定账号
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            String lockKey = LOCK_PREFIX + key;
            stringRedisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("登录失败次数过多，账号已锁定: key={}, attempts={}, lockMinutes={}",
                    key, attempts, LOCK_DURATION_MINUTES);
        }
    }

    @Override
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }

    @Override
    public void clearAttempts(String key) {
        String attemptKey = ATTEMPT_PREFIX + key;
        String lockKey = LOCK_PREFIX + key;
        stringRedisTemplate.delete(attemptKey);
        stringRedisTemplate.delete(lockKey);
    }

    @Override
    public long getRemainingLockSeconds(String key) {
        String lockKey = LOCK_PREFIX + key;
        Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
