package com.petcare.system.service;

public interface VerificationCodeService {

    /**
     * 发送验证码到指定手机号
     * @param phone 手机号
     * @return 发送的验证码（开发/测试环境返回，生产环境通过短信发送）
     */
    String sendCode(String phone);

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code  验证码
     * @return 验证是否通过
     */
    boolean verifyCode(String phone, String code);

    /**
     * 校验后删除验证码（一次性使用）
     * @param phone 手机号
     * @param code  验证码
     * @return 验证是否通过
     */
    boolean verifyAndConsumeCode(String phone, String code);
}
