package com.complycore.compliance.compliance_tool.service;

import com.complycore.compliance.compliance_tool.entity.CheckStatus;
import com.complycore.compliance.compliance_tool.entity.ComplianceCheck;
import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final ComplianceRuleService ruleService;
    private final ComplianceCheckService checkService;

    public ReportService(ComplianceRuleService ruleService, ComplianceCheckService checkService) {
        this.ruleService = ruleService;
        this.checkService = checkService;
    }

    public Map<String, Object> generateComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<ComplianceRule> rules = ruleService.findAll();
        List<ComplianceCheck> checks = checkService.findAll();

        // Filter checks by date range
        if (startDate != null && endDate != null) {
            checks = checks.stream()
                    .filter(check -> check.getCheckedAt() != null &&
                            !check.getCheckedAt().isBefore(startDate) &&
                            !check.getCheckedAt().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalRules", rules.size());

        // Status Summary
        Map<String, Integer> statusSummary = new HashMap<>();
        statusSummary.put("COMPLIANT", 0);
        statusSummary.put("NON_COMPLIANT", 0);
        statusSummary.put("PENDING", 0);

        // Category Summary
        Map<String, Integer> categorySummary = new HashMap<>();
        // Severity Summary
        Map<String, Integer> severitySummary = new HashMap<>();
        // Detailed Rules Data
        List<Map<String, Object>> detailedRules = rules.stream().map(rule -> {
            Map<String, Object> ruleData = new HashMap<>();
            String latestStatus = getLatestStatus(rule.getChecks(), startDate, endDate);
            ruleData.put("id", rule.getId());
            ruleData.put("name", rule.getName());
            ruleData.put("category", rule.getCategory());
            ruleData.put("severity", rule.getSeverity().toString());
            ruleData.put("status", latestStatus);
            ComplianceCheck latestCheck = getLatestCheck(rule.getChecks(), startDate, endDate);
            ruleData.put("lastChecked", latestCheck != null ? latestCheck.getCheckedAt() : null);
            return ruleData;
        }).collect(Collectors.toList());

        for (ComplianceRule rule : rules) {
            String latestStatus = getLatestStatus(rule.getChecks(), startDate, endDate);
            statusSummary.put(latestStatus, statusSummary.get(latestStatus) + 1);
            categorySummary.put(rule.getCategory(), categorySummary.getOrDefault(rule.getCategory(), 0) + 1);
            severitySummary.put(rule.getSeverity().toString(), severitySummary.getOrDefault(rule.getSeverity().toString(), 0) + 1);
        }

        report.put("statusSummary", statusSummary);
        report.put("categorySummary", categorySummary);
        report.put("severitySummary", severitySummary);
        report.put("totalChecks", checks.size());
        report.put("detailedRules", detailedRules);
        report.put("lastUpdated", LocalDateTime.now());

        return report;
    }

    private String getLatestStatus(List<ComplianceCheck> checks, LocalDateTime startDate, LocalDateTime endDate) {
        ComplianceCheck latest = getLatestCheck(checks, startDate, endDate);
        return latest != null ? latest.getStatus().toString() : "PENDING";
    }

    private ComplianceCheck getLatestCheck(List<ComplianceCheck> checks, LocalDateTime startDate, LocalDateTime endDate) {
        if (checks == null || checks.isEmpty()) return null;
        return checks.stream()
                .filter(check -> check.getCheckedAt() != null &&
                        (startDate == null || !check.getCheckedAt().isBefore(startDate)) &&
                        (endDate == null || !check.getCheckedAt().isAfter(endDate)))
                .max((c1, c2) -> c1.getCheckedAt().compareTo(c2.getCheckedAt()))
                .orElse(null);
    }
}