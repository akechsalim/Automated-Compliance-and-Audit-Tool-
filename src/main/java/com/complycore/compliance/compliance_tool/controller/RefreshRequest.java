package com.complycore.compliance.compliance_tool.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
