package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.AuditLog;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@AuthenticationPrincipal CustomUserDetails principal) {
        auditLogService.log(principal.getUsername(), "AUDIT_LOGS_VIEWED", "Viewed audit logs");
        List<AuditLog> logs = auditLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}