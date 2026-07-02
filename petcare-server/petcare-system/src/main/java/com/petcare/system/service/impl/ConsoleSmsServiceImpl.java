package com.petcare.system.service.impl;

import com.petcare.system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * 控制台输出短信服务（开发环境默认实现）。
 * <p>
 * 将短信内容输出到日志控制台，不实际发送。
 * 接入真实短信SDK时，创建 AliSmsService / TencentSmsService 实现 SmsService 接口，
 * 并标注 @Primary 或使用 @ConditionalOnProperty 切换。
 *
 * @see SmsService
 */
@Slf4j
@Service
@ConditionalOnMissingBean(value = SmsService.class, ignored = ConsoleSmsServiceImpl.class)
public class ConsoleSmsServiceImpl implements SmsService {

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        log.info("===== Mock短信验证码 =====");
        log.info("手机号: {}", maskPhone(phone));
        log.info("验证码: {}", code);
        log.info("==========================");
        return true;
    }

    @Override
    public boolean sendNotification(String phone, String content) {
        log.info("===== Mock通知短信 =====");
        log.info("手机号: {}", maskPhone(phone));
        log.info("内容: {}", content);
        log.info("=========================");
        return true;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
