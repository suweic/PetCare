package com.petcare.system.service.impl;

import com.petcare.common.BusinessException;
import com.petcare.system.service.SmsService;
import com.petcare.system.service.VerificationCodeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String CODE_PREFIX = "sms:code:";
    private static final String LIMIT_PREFIX = "sms:limit:";
    private static final String IP_LIMIT_PREFIX = "sms:ip_limit:";
    private static final String CODE_ATTEMPT_PREFIX = "sms:attempt:";
    private static final long CODE_EXPIRE_SECONDS = 300;  // 验证码5分钟有效
    private static final long LIMIT_SECONDS = 60;          // 60秒内不可重复发送
    private static final int CODE_LENGTH = 6;

    /** 同一 IP 每分钟最多发送次数 */
    private static final int IP_MAX_REQUESTS = 3;
    private static final long IP_LIMIT_SECONDS = 60;

    /** 同一验证码最大尝试验证次数（防暴力破解） */
    private static final int MAX_CODE_ATTEMPTS = 5;

    private final StringRedisTemplate stringRedisTemplate;
    private final SmsService smsService;

    @Override
    public String sendCode(String phone) {
        String limitKey = LIMIT_PREFIX + phone;

        // 检查60秒内是否已发送
        Boolean hasLimit = stringRedisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(hasLimit)) {
            Long ttl = stringRedisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
            long remaining = ttl != null ? ttl : 0;
            throw BusinessException.badRequest("验证码已发送，请" + remaining + "秒后再试");
        }

        // IP 级别频率限制（防止攻击者用不同手机号绕过 per-phone 限制）
        String clientIp = getClientIp();
        String ipLimitKey = IP_LIMIT_PREFIX + clientIp;
        Long ipCount = stringRedisTemplate.opsForValue().increment(ipLimitKey);
        if (ipCount != null && ipCount == 1) {
            stringRedisTemplate.expire(ipLimitKey, IP_LIMIT_SECONDS, TimeUnit.SECONDS);
        }
        if (ipCount != null && ipCount > IP_MAX_REQUESTS) {
            throw BusinessException.badRequest("操作过于频繁，请稍后再试");
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 存储验证码（5分钟过期）
        String codeKey = CODE_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 设置发送频率限制（60秒过期）
        stringRedisTemplate.opsForValue().set(limitKey, "1", LIMIT_SECONDS, TimeUnit.SECONDS);

        log.info("验证码已发送: phone={}, ip={}", maskPhone(phone), clientIp);

        // 实际发送短信（开发环境输出到控制台，生产环境对接阿里云/腾讯云SDK）
        try {
            smsService.sendVerificationCode(phone, code);
        } catch (Exception e) {
            log.error("短信发送失败: phone={}", maskPhone(phone), e);
            // 非致命错误：验证码已存入Redis，用户可重试或管理员可通过日志获取
        }

        return code;
    }

    /**
     * 获取客户端真实 IP。
     * <p>
     * 优先使用 request.getRemoteAddr()（无法伪造），适用于非代理环境。
     * 如需在反向代理后获取真实 IP，请配置代理并改用 X-Forwarded-For（带可信代理校验）。
     * </p>
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "127.0.0.1";
            }
            HttpServletRequest request = attrs.getRequest();

            String remoteAddr = request.getRemoteAddr();
            if (remoteAddr != null && !remoteAddr.isEmpty()) {
                return remoteAddr;
            }
            return "127.0.0.1";
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
            return "127.0.0.1";
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String codeKey = CODE_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        return storedCode != null && storedCode.equals(code);
    }

    @Override
    public boolean verifyAndConsumeCode(String phone, String code) {
        String codeKey = CODE_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            return false;
        }

        // 验证码尝试次数限制（防暴力破解）
        String attemptKey = CODE_ATTEMPT_PREFIX + phone;
        Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            // 首次尝试：TTL 与验证码有效期对齐
            Long codeTtl = stringRedisTemplate.getExpire(codeKey);
            if (codeTtl != null && codeTtl > 0) {
                stringRedisTemplate.expire(attemptKey, codeTtl, java.util.concurrent.TimeUnit.SECONDS);
            }
        }

        if (attempts != null && attempts > MAX_CODE_ATTEMPTS) {
            // 超过最大尝试次数：删除验证码，强制重新获取
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(attemptKey);
            log.warn("验证码尝试次数超限 (>{})：phone={}, 已作废验证码", MAX_CODE_ATTEMPTS, phone);
            return false;
        }

        if (!storedCode.equals(code)) {
            return false;
        }

        // 验证码一次性使用，验证通过后删除
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(attemptKey);
        log.info("验证码校验通过并已消费: phone={}", phone);
        return true;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 手机号脱敏：保留前3位和后4位。
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
