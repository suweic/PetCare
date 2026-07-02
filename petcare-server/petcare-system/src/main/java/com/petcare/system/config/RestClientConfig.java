package com.petcare.system.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/**
 * RestClient 全局配置：超时、连接池
 * <p>
 * LLM API 调用等外部 HTTP 请求均通过此配置获得超时保护。
 *
 * @see com.petcare.system.service.impl.PreConsultationServiceImpl
 */
@Configuration
public class RestClientConfig {

    /**
     * 连接超时: 10 秒（建立 TCP 连接）
     * 读取超时: 60 秒（等待 LLM 响应，含推理时间）
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }
}
