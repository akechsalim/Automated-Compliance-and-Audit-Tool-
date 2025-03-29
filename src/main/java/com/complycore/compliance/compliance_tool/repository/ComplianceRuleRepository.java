package com.complycore.compliance.compliance_tool.repository;

import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, Long> {
}