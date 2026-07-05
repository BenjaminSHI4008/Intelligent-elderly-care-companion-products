package com.xiaoban.server.security;

import com.xiaoban.server.config.properties.JwtAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtAuthProperties props = new JwtAuthProperties();
        props.setSecret("test-jwt-secret-key-at-least-32-chars-long");
        props.setExpirationMs(3_600_000L);
        jwtUtil = new JwtUtil(props);
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
