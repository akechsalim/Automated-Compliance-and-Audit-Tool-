package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.ComplianceCheck;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import com.complycore.compliance.compliance_tool.service.ComplianceCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checks")
public class ComplianceCheckController {
    private final ComplianceCheckService checkService;
    private final AuditLogService auditLogService;

    public ComplianceCheckController(ComplianceCheckService checkService, AuditLogService auditLogService) {
        this.checkService = checkService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/rule/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceCheck> createCheck(@PathVariable Long ruleId,
                                                       @RequestBody ComplianceCheck check,
                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        ComplianceCheck createdCheck = checkService.createCheck(ruleId, check);
        auditLogService.log(principal.getUsername(), "CHECK_CREATED", "Check for rule: " + ruleId);
        return ResponseEntity.ok(createdCheck);
    }

    @GetMapping("/rule/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<List<ComplianceCheck>> getChecksByRule(@PathVariable Long ruleId,
                                                                 @AuthenticationPrincipal CustomUserDetails principal) {
        auditLogService.log(principal.getUsername(), "CHECKS_VIEWED", "Checks for rule: " + ruleId);
        return ResponseEntity.ok(checkService.findByRuleId(ruleId));
    }

    @PutMapping("/{checkId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceCheck> updateCheck(@PathVariable Long checkId,
                                                       @RequestBody ComplianceCheck check,
                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        ComplianceCheck updatedCheck = checkService.updateCheck(checkId, check);
        auditLogService.log(principal.getUsername(), "CHECK_UPDATED", "Check ID: " + checkId);
        return ResponseEntity.ok(updatedCheck);
    }
}