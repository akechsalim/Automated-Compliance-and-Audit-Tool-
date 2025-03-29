package com.complycore.compliance.compliance_tool.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        blacklistService = new TokenBlacklistService();
    }

    @Test
    void testBlacklistAndCheckToken() {
        String token = "test.token.123";
        blacklistService.blacklistToken(token);
        assertTrue(blacklistService.isTokenBlacklisted(token));
    }

    @Test
    void testNonBlacklistedToken() {
        String token = "test.token.123";
        assertFalse(blacklistService.isTokenBlacklisted(token));
    }
}