package com.complycore.compliance.compliance_tool.repository;

import com.complycore.compliance.compliance_tool.entity.ComplianceCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, Long> {
    List<ComplianceCheck> findByRuleId(Long ruleId);
}