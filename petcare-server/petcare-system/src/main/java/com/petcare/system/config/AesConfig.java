package com.petcare.system.config;

import com.petcare.common.util.AESEncryptUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * AES 加密配置桥接。
 *
 * <p>将 Spring 配置文件中的 {@code aes.key} 注入到 {@link AESEncryptUtil} 工具类中，
 * 确保 MyBatis TypeHandler（{@code EncryptedStringTypeHandler}）能正确使用 Spring
 * 统一配置的密钥进行加解密，而非依赖环境变量 {@code AES_KEY}。</p>
 *
 * <h3>配置优先级</h3>
 * <ol>
 *   <li>Spring {@code aes.key} 配置（来自 application.yml 或环境变量）</li>
 *   <li>{@code System.getenv("AES_KEY")} 回退（静态初始化块中）</li>
 * </ol>
 *
 * <p>密钥格式：32 字节 Base64 编码字符串（AES-256-GCM）</p>
 */
@Slf4j
@Configuration
public class AesConfig {

    @Value("${aes.key:}")
    private String aesKey;

    @PostConstruct
    public void init() {
        if (aesKey == null || aesKey.isBlank() || "changeme".equals(aesKey)) {
            // Spring 未配置有效密钥，检查环境变量回退
            String envKey = System.getenv("AES_KEY");
            if (envKey != null && !envKey.isBlank()) {
                log.warn("aes.key 未在 Spring 配置中设置，使用环境变量 AES_KEY 作为回退。"
                        + "建议在 application.yml 中配置 aes.key 以统一管理。");
            } else {
                log.warn("AES 加密密钥未配置。请在 application.yml 中设置 aes.key"
                        + " 或设置环境变量 AES_KEY。"
                        + "敏感个人信息的加密存储将不可用。");
            }
            return;
        }

        try {
            AESEncryptUtil.configureWithSpring(aesKey);
            log.info("AES 加密密钥已从 Spring 配置加载");
        } catch (IllegalArgumentException e) {
            log.error("Spring 配置中的 aes.key 无效: {}", e.getMessage());
        }
    }
}
