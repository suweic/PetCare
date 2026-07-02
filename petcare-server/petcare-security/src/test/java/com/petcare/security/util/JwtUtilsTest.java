package com.petcare.security.util;

import com.petcare.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("petcare-jwt-secret-key-at-least-32-bytes-long");
        properties.setExpiration(3600L);
        jwtUtils = new JwtUtils(properties);
    }

    @Test
    void shouldGenerateAndParseTokenSuccessfully() {
        String token = jwtUtils.generateToken(1L, 1);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));

        Claims claims = jwtUtils.parseToken(token);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals(1, claims.get("userType", Integer.class));
    }

    @Test
    void shouldExtractUserInfoFromToken() {
        String token = jwtUtils.generateToken(2L, 2);

        assertEquals(2L, jwtUtils.getUserId(token));
        assertEquals(2, jwtUtils.getUserType(token));
    }

    @Test
    void shouldInvalidateExpiredToken() throws InterruptedException {
        JwtProperties shortLivedProperties = new JwtProperties();
        shortLivedProperties.setSecret("petcare-jwt-secret-key-at-least-32-bytes-long");
        shortLivedProperties.setExpiration(1L);
        JwtUtils shortLivedJwtUtils = new JwtUtils(shortLivedProperties);

        String token = shortLivedJwtUtils.generateToken(1L, 1);
        assertTrue(shortLivedJwtUtils.validateToken(token));

        Thread.sleep(1500);
        assertFalse(shortLivedJwtUtils.validateToken(token));
    }

    @Test
    void shouldInvalidateMalformedToken() {
        assertFalse(jwtUtils.validateToken("invalid-token"));
    }
}
