package com.petcare.system.service.impl;

import com.petcare.common.BusinessException;
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
    private static final long CODE_EXPIRE_SECONDS = 300;  // 验证码5分钟有效
    private static final long LIMIT_SECONDS = 60;          // 60秒内不可重复发送
    private static final int CODE_LENGTH = 6;

    /** 同一 IP 每分钟最多发送次数 */
    private static final int IP_MAX_REQUESTS = 3;
    private static final long IP_LIMIT_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;

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

        log.info("验证码已发送: phone={}, ip={}", phone, clientIp);
        // TODO: 生产环境对接真实短信服务（阿里云/腾讯云短信SDK）
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

        if (!storedCode.equals(code)) {
            return false;
        }

        // 验证码一次性使用，验证通过后删除
        stringRedisTemplate.delete(codeKey);
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
}
