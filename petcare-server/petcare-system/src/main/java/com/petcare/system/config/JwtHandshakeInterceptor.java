package com.petcare.system.config;

import com.petcare.security.service.TokenBlacklistService;
import com.petcare.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 握手拦截器 — JWT 鉴权。
 *
 * <p>在 WebSocket 握手阶段从请求参数中提取 JWT Token 并验证，
 * 验证通过后将用户信息设置为 WebSocket Session 的 Principal，
 * 失败则拒绝握手。</p>
 *
 * <h3>客户端连接方式</h3>
 * <pre>{@code
 * // 原生 WebSocket
 * new WebSocket("ws://host:8080/ws?token=eyJhbG...");
 *
 * // SockJS / STOMP
 * const client = Stomp.client("ws://host:8080/ws");
 * client.connect({ token: "eyJhbG..." }, ...);
 * // 或作为 URL 参数: const client = Stomp.client("ws://host:8080/ws?token=eyJhbG...");
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket 握手失败: 缺少 Token");
            return false;
        }

        try {
            // 验证 Token 有效性
            if (!jwtUtils.validateToken(token)) {
                log.warn("WebSocket 握手失败: Token 无效或已过期");
                return false;
            }

            // 黑名单检查
            String jti = jwtUtils.getTokenId(token);
            if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                log.warn("WebSocket 握手失败: Token 已被撤销, jti={}", jti);
                return false;
            }

            // 解析用户信息
            Claims claims = jwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            Integer userType = claims.get("userType", Integer.class);

            if (userId == null || userType == null) {
                log.warn("WebSocket 握手失败: Token 缺少必要声明");
                return false;
            }

            // 将用户信息存入 WebSocket session attributes，供后续使用
            attributes.put("userId", userId);
            attributes.put("userType", userType);
            attributes.put("token", token);

            // 设置 Principal（使 SimpMessagingTemplate.convertAndSendToUser 可用）
            // 注意: 这里通过 attributes 设置，需配合 WebSocketConfig 的 setUserDestinationPrefix
            StompPrincipal principal = new StompPrincipal(userId.toString());
            attributes.put("principal", principal);

            log.debug("WebSocket 握手成功: userId={}, userType={}", userId, userType);
            return true;

        } catch (Exception e) {
            log.warn("WebSocket 握手失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后无操作
    }

    /**
     * 从请求中提取 JWT Token。
     * 优先从 URL 参数 "token" 提取，其次从 HTTP Header "Authorization" 提取。
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. 尝试从 URL 参数提取: /ws?token=xxx
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(token)) {
                return token;
            }
        } else {
            // 非 Servlet 环境下从 URI 参数提取
            String query = request.getURI().getQuery();
            if (StringUtils.hasText(query)) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && "token".equals(pair[0])) {
                        return pair[1];
                    }
                }
            }
        }

        // 2. 尝试从 STOMP CONNECT 帧的 Header "Authorization" 提取
        //    (客户端通过 SockJS connect 时传递的 header)
        //    Spring 在握手阶段会将 STOMP CONNECT header 放在 attributes 中，
        //    但此处 beforeHandshake 早于 STOMP 帧解析。
        //    对于 SockJS 场景，建议客户端将 token 作为 URL 参数传递。

        return null;
    }

    /**
     * WebSocket 会话 Principal 实现。
     * 用户 ID 作为 name，支持 {@code convertAndSendToUser(userId, ...)} 一对一推送。
     */
    record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
