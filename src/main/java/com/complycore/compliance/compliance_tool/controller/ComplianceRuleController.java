package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import com.complycore.compliance.compliance_tool.service.ComplianceRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class ComplianceRuleController {
    private final ComplianceRuleService ruleService;
    private final AuditLogService auditLogService;

    public ComplianceRuleController(ComplianceRuleService ruleService, AuditLogService auditLogService) {
        this.ruleService = ruleService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceRule> createRule(@RequestBody ComplianceRule rule,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        ComplianceRule createdRule = ruleService.createRule(rule);
        auditLogService.log(principal.getUsername(), "RULE_CREATED", "Rule: " + rule.getName());
        return ResponseEntity.ok(createdRule);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<List<ComplianceRule>> getAllRules(@AuthenticationPrincipal CustomUserDetails principal) {
        auditLogService.log(principal.getUsername(), "RULES_VIEWED", "Retrieved all rules");
        return ResponseEntity.ok(ruleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<ComplianceRule> getRuleById(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return ruleService.findById(id)
                .map(rule -> {
                    auditLogService.log(principal.getUsername(), "RULE_VIEWED", "Rule ID: " + id);
                    return ResponseEntity.ok(rule);
                })
                .orElseGet(() -> {
                    auditLogService.log(principal.getUsername(), "RULE_VIEW_FAILED", "Rule not found: " + id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceRule> updateRule(@PathVariable Long id,
                                                     @RequestBody ComplianceRule rule,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        ComplianceRule updatedRule = ruleService.updateRule(id, rule);
        auditLogService.log(principal.getUsername(), "RULE_UPDATED", "Rule ID: " + id);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRule(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails principal) {
        ruleService.deleteRule(id);
        auditLogService.log(principal.getUsername(), "RULE_DELETED", "Rule ID: " + id);
        return ResponseEntity.ok().build();
    }
}