package com.petcare.system.service;

/**
 * 短信服务接口。
 * <p>
 * 当前为接口定义。接入阿里云/腾讯云/其他短信 SDK 时，
 * 创建对应的 {@code SmsServiceImpl} 实现此接口。
 * <p>
 * 配置示例（阿里云短信）：
 * <pre>
 * sms:
 *   provider: aliyun
 *   access-key-id: ${SMS_ACCESS_KEY_ID}
 *   access-key-secret: ${SMS_ACCESS_KEY_SECRET}
 *   sign-name: PetCare
 *   template-code: SMS_123456789
 * </pre>
 */
public interface SmsService {

    /**
     * 发送短信验证码。
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone, String code);

    /**
     * 发送通知短信。
     *
     * @param phone   手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean sendNotification(String phone, String content);
}
