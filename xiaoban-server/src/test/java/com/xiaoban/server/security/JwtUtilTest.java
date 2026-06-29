package com.xiaoban.server.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 通过反射注入 secret（@Value 在单元测试中不生效）
        org.springframework.test.util.ReflectionTestUtils.setField(
                jwtUtil, "secret", "test-jwt-secret-key-at-least-32-chars-long");
        org.springframework.test.util.ReflectionTestUtils.setField(
                jwtUtil, "expiration", 3600000L);
    }

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken(42L, "elder");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(42L, jwtUtil.getUserId(token));
        assertEquals("elder", jwtUtil.getRole(token));
    }

    @Test
    void rejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}
