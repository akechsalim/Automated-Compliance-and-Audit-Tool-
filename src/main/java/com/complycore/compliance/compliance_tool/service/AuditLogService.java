package com.complycore.compliance.compliance_tool.service;

import com.complycore.compliance.compliance_tool.entity.AuditLog;
import com.complycore.compliance.compliance_tool.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;


    public AuditLogService(AuditLogRepository auditLogRepository, HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.request = request;
    }

    public void log(String username, String action, String details) {
        String ipAddress = request.getRemoteAddr();
        AuditLog log = new AuditLog(username, action, details, LocalDateTime.now(), ipAddress);
        auditLogRepository.save(log);
    }
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}