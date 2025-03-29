package com.complycore.compliance.compliance_tool.service;

import com.complycore.compliance.compliance_tool.entity.ComplianceCheck;
import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import com.complycore.compliance.compliance_tool.repository.ComplianceCheckRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplianceCheckService {
    private final ComplianceCheckRepository checkRepository;
    private final ComplianceRuleService ruleService;

    public ComplianceCheckService(ComplianceCheckRepository checkRepository, ComplianceRuleService ruleService) {
        this.checkRepository = checkRepository;
        this.ruleService = ruleService;
    }

    public ComplianceCheck createCheck(Long ruleId, ComplianceCheck check) {
        ComplianceRule rule = ruleService.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        check.setRule(rule);
        check.setCheckedAt(LocalDateTime.now());
        return checkRepository.save(check);
    }

    public List<ComplianceCheck> findByRuleId(Long ruleId) {
        return checkRepository.findByRuleId(ruleId);
    }

    public List<ComplianceCheck> findAll() { // New method
        return checkRepository.findAll();
    }

    public ComplianceCheck updateCheck(Long checkId, ComplianceCheck checkUpdates) {
        ComplianceCheck check = checkRepository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("Check not found"));
        check.setDescription(checkUpdates.getDescription());
        check.setStatus(checkUpdates.getStatus());
        check.setCheckedAt(checkUpdates.getCheckedAt());
        return checkRepository.save(check);
    }
}