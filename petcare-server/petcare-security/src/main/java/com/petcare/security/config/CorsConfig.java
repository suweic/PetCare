package com.petcare.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * 允许的跨域来源，可通过配置覆盖。多个域名用逗号分隔。
     * 默认允许本地开发环境的常见端口。
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * 允许的跨域来源模式（支持通配符），优先级高于 allowed-origins。
     * 注意：allowCredentials=true 时不能使用 "*"，必须指定具体模式。
     */
    @Value("${cors.allowed-origin-patterns:http://localhost:*}")
    private String allowedOriginPatterns;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 解析配置的来源模式
        List<String> originPatterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOriginPatterns(originPatterns);

        // 精确来源作为补充
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 注意：allowCredentials=true 时不允许使用 "*" 作为 allowedHeaders
        // CORS 规范要求凭据模式下必须列出明确的头部名称
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Cache-Control"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
