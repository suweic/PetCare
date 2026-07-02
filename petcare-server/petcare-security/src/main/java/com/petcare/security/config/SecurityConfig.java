package com.petcare.security.config;

import com.petcare.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/user/**", "/api/doctor/**", "/api/consultation/**", "/api/order/**", "/api/pet/**", "/api/department/**", "/api/upload/**", "/api/prescription/**", "/api/evaluation/**", "/api/pre-consultation/**")
                // CSRF 已禁用：本系统使用 JWT Bearer Token 进行认证（通过 Authorization header 传递），
                // 不依赖浏览器 Cookie 存储会话，因此不存在 CSRF 攻击面。
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/register",
                                "/api/user/send-code",
                                "/api/user/login-by-code",
                                "/api/doctor/register",
                                "/api/doctor/login",
                                "/api/doctor/list",
                                "/api/department/list"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 安全响应头
                .headers(headers -> headers
                        .contentTypeOptions(cfg -> {})       // X-Content-Type-Options: nosniff
                        .frameOptions(cfg -> cfg.deny())     // X-Frame-Options: DENY (防点击劫持)
                        .xssProtection(cfg -> cfg.disable())  // 禁用旧版XSS过滤器，依赖CSP
                );

        return http.build();
    }

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/admin/**")
                // CSRF 已禁用：理由同上，Admin 后台同样使用 JWT Bearer Token
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 安全响应头
                .headers(headers -> headers
                        .contentTypeOptions(cfg -> {})
                        .frameOptions(cfg -> cfg.deny())
                        .xssProtection(cfg -> cfg.disable())
                );

        return http.build();
    }
}
