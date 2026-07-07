package com.petcare.system.service.impl;

import com.petcare.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * LLM API 客户端 — 独立的 {@link Service} 使 Spring AOP 代理能够拦截 {@link Retryable}。
 *
 * <p>从 {@link PreConsultationServiceImpl} 中提取，解决两个关键问题：
 * <ol>
 *   <li><b>Self-invocation</b>: 原 {@code callLLM()} 在同类内部调用，绕过代理，
 *       {@code @Retryable} 从不触发</li>
 *   <li><b>RestClient 超时</b>: 原实现用 {@code RestClient.builder()} 静态工厂，
 *       绕过 {@code RestClientCustomizer} 配置的连接(10s)和读取(60s)超时</li>
 * </ol>
 */
@Slf4j
@Service
public class LlmApiClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${llm.api.url:}")
    private String llmApiUrl;

    @Value("${llm.api.key:}")
    private String llmApiKey;

    @Value("${llm.api.model:gpt-4o-mini}")
    private String llmModel;

    public LlmApiClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    /**
     * 返回当前配置的 LLM 模型名称，供 PreConsultationServiceImpl 等调用方记录。
     */
    public String getLlmModel() {
        return llmModel;
    }

    /**
     * 调用 LLM API（OpenAI 兼容接口），支持自动重试。
     *
     * @param prompt 提示词
     * @return LLM 原始 JSON 响应
     * @throws RuntimeException 所有重试耗尽后抛出
     */
    @Retryable(
            retryFor = {RuntimeException.class},
            noRetryFor = {BusinessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 8000),
            label = "llm-api-call"
    )
    public String call(String prompt) {
        // 使用注入的 RestClient.Builder（自动应用 RestClientCustomizer 的超时配置）
        RestClient restClient = restClientBuilder
                .baseUrl(llmApiUrl)
                .defaultHeader("Authorization", "Bearer " + llmApiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", llmModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "你是一位专业的宠物医疗分诊助手。你只返回JSON，不返回其他内容。"),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 1000
        );

        log.info("调用LLM API: model={}, url={}", llmModel, llmApiUrl);
        String response = restClient.post()
                .body(requestBody)
                .retrieve()
                .onStatus(status -> status.value() >= 500, (req, resp) -> {
                    throw new RuntimeException("LLM API 服务器错误: HTTP " + resp.getStatusCode());
                })
                .onStatus(status -> status.value() == 429, (req, resp) -> {
                    throw new RuntimeException("LLM API 限流 (429)，将自动重试");
                })
                .body(String.class);

        log.debug("LLM原始响应: {}", response);
        return response;
    }

    /**
     * 重试耗尽后的兜底处理。
     */
    @Recover
    private String fallback(RuntimeException e, String prompt) {
        log.error("LLM API调用全部重试失败 (maxAttempts=3)", e);
        throw BusinessException.badRequest(
                "AI分析服务暂时不可用，请稍后重试。如需紧急帮助请联系客服。");
    }
}
