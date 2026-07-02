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