package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.Role;
import com.complycore.compliance.compliance_tool.entity.User;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.security.CustomUserDetailsService;
import com.complycore.compliance.compliance_tool.security.JwtUtil;
import com.complycore.compliance.compliance_tool.security.TokenBlacklistService;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import com.complycore.compliance.compliance_tool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditLogService auditLogService;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, UserService userService, TokenBlacklistService tokenBlacklistService, AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            auditLogService.log(loginRequest.getUsername(), "LOGIN", "User logged in successfully");
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "Login successful"));
        } catch (BadCredentialsException e) {
            auditLogService.log(loginRequest.getUsername(), "LOGIN_FAILED", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setEmail(registerRequest.getEmail());
            user.setRole(Role.EMPLOYEE);
            userService.createUser(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            auditLogService.log(user.getUsername(), "REGISTER", "User registered successfully");
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "Registration successful"));
        } catch (Exception e) {
            auditLogService.log(registerRequest.getUsername(), "REGISTER_FAILED", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            if (!"refresh".equals(jwtUtil.extractTokenType(refreshToken))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(null, null, "Invalid refresh token"));
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtUtil.generateAccessToken(userDetails);
                auditLogService.log(username, "TOKEN_REFRESH", "Access token refreshed");
                return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken, "Token refreshed"));
            }
            auditLogService.log(username, "TOKEN_REFRESH_FAILED", "Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid or expired refresh token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Token refresh failed: " + e.getMessage()));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String authorizationHeader,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            auditLogService.log(userDetails.getUsername(), "LOGOUT", "User logged out");
            return ResponseEntity.ok(new AuthResponse(null, null, "Logged out successfully"));
        }
        auditLogService.log(userDetails != null ? userDetails.getUsername() : null, "LOGOUT_FAILED", "Invalid token format");
        return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Invalid token format"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetails> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userDetails);
    }
}

