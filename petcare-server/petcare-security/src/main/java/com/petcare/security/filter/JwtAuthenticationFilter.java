package com.petcare.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.security.service.TokenBlacklistService;
import com.petcare.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            if (!jwtUtils.validateToken(token)) {
                sendUnauthorizedResponse(response, "Token无效或已过期");
                return;
            }

            // 黑名单检查：已登出的 Token 拒绝使用
            try {
                String jti = jwtUtils.getTokenId(token);
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.warn("Token 已被撤销（黑名单中）: jti={}", jti);
                    sendUnauthorizedResponse(response, "Token已被撤销，请重新登录");
                    return;
                }
            } catch (Exception e) {
                log.warn("Token 黑名单检查失败: {}", e.getMessage());
                sendUnauthorizedResponse(response, "认证失败");
                return;
            }

            try {
                Claims claims = jwtUtils.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                Integer userType = claims.get("userType", Integer.class);

                // 拒绝缺少必要声明的 token
                if (userId == null || userType == null) {
                    log.warn("JWT缺少必要声明: userId={}, userType={}", userId, userType);
                    sendUnauthorizedResponse(response, "Token无效：缺少必要信息");
                    return;
                }

                List<SimpleGrantedAuthority> authorities = resolveAuthorities(userType);

                Authentication authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT认证成功, userId={}, userType={}", userId, userType);
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                sendUnauthorizedResponse(response, "认证失败");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        body.put("timestamp", System.currentTimeMillis());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private List<SimpleGrantedAuthority> resolveAuthorities(Integer userType) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (userType == null) {
            return authorities;
        }

        switch (userType) {
            case 1 -> authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            case 2 -> authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
            case 3 -> authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            default -> log.warn("未知的用户类型: {}", userType);
        }

        return authorities;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
