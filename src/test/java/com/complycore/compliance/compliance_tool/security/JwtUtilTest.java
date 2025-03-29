package com.complycore.compliance.compliance_tool.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testsecretkey12345678901234567890"); // 32+ chars for HS512
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", 3600000);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationInMs", 604800000);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testuser", "password", Collections.emptyList());
        String token = jwtUtil.generateAccessToken(userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertEquals("access", jwtUtil.extractTokenType(token));
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testuser", "password", Collections.emptyList());
        String token = jwtUtil.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertEquals("refresh", jwtUtil.extractTokenType(token));
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testInvalidToken() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testuser", "password", Collections.emptyList());
        String token = "invalid.token.here";
        assertFalse(jwtUtil.validateToken(token, userDetails));
    }
}