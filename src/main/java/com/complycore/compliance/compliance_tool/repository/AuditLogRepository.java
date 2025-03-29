package com.complycore.compliance.compliance_tool.repository;

import com.complycore.compliance.compliance_tool.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}