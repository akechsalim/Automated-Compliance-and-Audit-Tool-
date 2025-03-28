package com.complycore.compliance.compliance_tool.controller;

import lombok.Getter;

@Getter
class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String message;

    public AuthResponse(String accessToken, String refreshToken, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
    }
}
