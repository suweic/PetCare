package com.petcare.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration;

    /**
     * 密钥轮换策略（Key Rotation）。
     *
     * <h3>JWT 密钥轮换</h3>
     * <ol>
     *   <li>生成新密钥并部署到环境变量（JWT_SECRET）</li>
     *   <li>过渡期：同时支持新旧密钥验证 Token（修改 JwtUtils 为多密钥验证）</li>
     *   <li>过渡期结束后移除旧密钥</li>
     *   <li>所有旧 Token 自然过期后完成轮换</li>
     * </ol>
     * 推荐过渡期为 Token 过期时间的 2 倍（当前 expiration=86400s=24h，建议过渡期 48h）。
     *
     * <h3>AES 密钥轮换</h3>
     * 更复杂：需要密钥版本号方案。
     * <ol>
     *   <li>加密数据时附加 version 字段（如 v1:AES_KEY_1, v2:AES_KEY_2）</li>
     *   <li>轮换时新增密钥版本，历史数据用旧版本密钥解密</li>
     *   <li>后台任务逐步用新密钥重新加密历史数据</li>
     *   <li>所有历史数据迁移完毕后移除旧版本密钥</li>
     * </ol>
     * 密钥轮换周期建议：90 天（符合 PCI DSS / ISO 27001 最佳实践）。
     */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException(
                    "JWT密钥未配置。请在环境变量中设置 JWT_SECRET，或在 application.yml 中配置 jwt.secret。"
                    + "密钥至少需要32个字符（HMAC-SHA256最低要求）。"
                    + "生成示例: openssl rand -base64 32");
        }
        if (secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT密钥长度不足（当前" + secret.length() + "字符，需要至少32字符）。"
                    + "请在环境变量 JWT_SECRET 或配置 jwt.secret 中设置更长的密钥。"
                    + "生成示例: openssl rand -base64 32");
        }
    }
}