package com.petcare.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket STOMP 配置。
 *
 * <h3>架构说明</h3>
 * <ul>
 *   <li>端点: {@code /ws} — 客户端通过此端点建立 WebSocket 连接，支持 SockJS 降级</li>
 *   <li>应用前缀: {@code /app} — 客户端发送消息到 {@code /app/chat.send} 等目标</li>
 *   <li>广播前缀: {@code /topic} — 服务端通过 {@code /topic/consultation/{id}} 广播消息</li>
 *   <li>用户前缀: {@code /user} — 服务端通过 {@code convertAndSendToUser} 一对一推送</li>
 *   <li>JWT 鉴权: {@link JwtHandshakeInterceptor} 在握手阶段验证 Token</li>
 * </ul>
 *
 * <h3>消息流</h3>
 * <pre>
 * 客户端 ───SEND /app/chat.send───> @MessageMapping("/chat.send") ───保存DB───> SimpMessagingTemplate
 *                                                                                │
 *                                          ┌─────────────────────────────────────┘
 *                                          ▼
 *                               /topic/consultation/{consultationId} (广播给问诊双方)
 * </pre>
 *
 * <h3>客户端连接示例</h3>
 * <pre>{@code
 * const socket = new SockJS('http://localhost:8080/ws?token=eyJhbG...');
 * const client = Stomp.over(socket);
 * client.connect({}, () => {
 *     // 订阅问诊消息
 *     client.subscribe('/topic/consultation/123', (msg) => {
 *         console.log(JSON.parse(msg.body));
 *     });
 *     // 发送消息
 *     client.send('/app/chat.send', {}, JSON.stringify({
 *         consultationId: 123,
 *         messageType: 1,
 *         content: "宠物最近食欲不振怎么办？"
 *     }));
 * });
 * }</pre>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${cors.allowed-origin-patterns:http://localhost:*}")
    private String allowedOriginPatterns;

    /**
     * 注册 STOMP 端点。
     * <p>客户端通过 {@code /ws} 建立连接，支持 SockJS 作为 WebSocket 不可用时的降级方案。</p>
     * <p>连接时需携带 JWT Token：{@code /ws?token=eyJhbG...}</p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler())
                .withSockJS();

        // 同时也注册一个不带 SockJS 的纯 WebSocket 端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler());
    }

    /**
     * 自定义握手处理器 — 从 Session Attributes 中提取 Principal，
     * 使 {@code convertAndSendToUser(userId, ...)} 能够正确路由。
     */
    private HandshakeHandler customHandshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                // JwtHandshakeInterceptor 已将 Principal 存入 attributes
                Principal principal = (Principal) attributes.get("principal");
                return principal != null ? principal : request.getPrincipal();
            }
        };
    }

    /**
     * 配置消息代理。
     * <ul>
     *   <li>{@code /topic}: 广播目的地 — 用于问诊房间内消息群发</li>
     *   <li>{@code /queue}: 用户私有队列 — 用于 {@code convertAndSendToUser} 一对一推送</li>
     *   <li>{@code /app}: 应用前缀 — 客户端发往 @MessageMapping 方法的前缀</li>
     *   <li>{@code /user}: 用户前缀 — {@code convertAndSendToUser} 映射到此前缀</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 内存消息代理 (可用 Redis 外置代理替换为生产级方案)
        registry.enableSimpleBroker("/topic", "/queue");

        // 客户端发往 /app/xxx 的消息路由到 @MessageMapping("/xxx")
        registry.setApplicationDestinationPrefixes("/app");

        // convertAndSendToUser 的路由前缀
        // 发送到 /user/{userId}/queue/... 的消息由 Stomp 自动路由给对应用户
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 配置客户端入站通道。
     * <p>此处不做额外拦截 — JWT 鉴权已在握手阶段完成。</p>
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 可在此处添加 ChannelInterceptor 做消息级权限校验
    }

    /**
     * 配置客户端出站通道。
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 可配置线程池大小以优化并发推送性能
    }
}
