package com.complycore.compliance.compliance_tool.service;

import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import com.complycore.compliance.compliance_tool.repository.ComplianceRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComplianceRuleService {
    private final ComplianceRuleRepository ruleRepository;

    public ComplianceRuleService(ComplianceRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public ComplianceRule createRule(ComplianceRule rule) {
        return ruleRepository.save(rule);
    }

    public List<ComplianceRule> findAll() {
        return ruleRepository.findAll();
    }

    public Optional<ComplianceRule> findById(Long id) {
        return ruleRepository.findById(id);
    }

    public ComplianceRule updateRule(Long id, ComplianceRule ruleUpdates) {
        ComplianceRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        rule.setName(ruleUpdates.getName());
        rule.setDescription(ruleUpdates.getDescription());
        rule.setActive(ruleUpdates.isActive());
        rule.setCategory(ruleUpdates.getCategory());
        rule.setSeverity(ruleUpdates.getSeverity());
        return ruleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
}