package com.petcare.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册限流拦截器和静态资源映射。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * 注册全局限流拦截器。
     * <p>
     * 拦截所有 /api/** 请求，按 IP + 每秒窗口限流。
     * 登录接口可配置更严格的限制（见 VerificationCodeServiceImpl IP 限流）。
     * </p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

    /**
     * 映射上传文件目录为静态资源，使上传的图片/文件可通过 URL 直接访问。
     * <p>
     * 示例：上传到 ./uploads/images/2026/07/01/abc.png 的文件，
     * 可通过 http://host:8080/uploads/images/2026/07/01/abc.png 访问。
     * </p>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
