package com.petcare.system.service;

/**
 * 登录失败次数限制服务，防止暴力破解
 */
public interface LoginAttemptService {

    /**
     * 记录一次登录失败
     * @param key 标识（手机号或用户名）
     */
    void recordFailedAttempt(String key);

    /**
     * 检查是否被锁定（连续失败次数超过阈值）
     * @param key 标识（手机号或用户名）
     * @return true=已锁定，false=正常
     */
    boolean isLocked(String key);

    /**
     * 登录成功后清除失败记录
     * @param key 标识（手机号或用户名）
     */
    void clearAttempts(String key);

    /**
     * 获取剩余锁定时间（秒）
     * @param key 标识（手机号或用户名）
     * @return 剩余秒数，未锁定返回0
     */
    long getRemainingLockSeconds(String key);
}
