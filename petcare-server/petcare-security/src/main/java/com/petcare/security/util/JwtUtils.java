/*
 * Copyright 2026 PetCare Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petcare.security.util;

import com.petcare.security.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, Integer userType) {
        Map<String, Object> claims = Map.of(
                "userId", userId,
                "userType", userType
        );

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .id(java.util.UUID.randomUUID().toString())  // jti: 支持黑名单撤销
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 提取 Token 的 JTI（唯一标识符），用于黑名单检查。
     */
    public String getTokenId(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    /**
     * 获取 Token 的过期时间（毫秒时间戳），用于黑名单 TTL 计算。
     */
    public long getTokenExpirationMillis(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw new RuntimeException("Token已过期");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            throw new RuntimeException("Token无效");
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public Integer getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType", Integer.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}