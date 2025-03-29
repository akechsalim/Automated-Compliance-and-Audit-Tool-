package com.complycore.compliance.compliance_tool.service;

import com.complycore.compliance.compliance_tool.entity.CheckStatus;
import com.complycore.compliance.compliance_tool.entity.ComplianceCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class ComplianceCheckScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ComplianceCheckScheduler.class);
    private final ComplianceCheckService checkService;

    public ComplianceCheckScheduler(ComplianceCheckService checkService) {
        this.checkService = checkService;
    }

    @Scheduled(fixedRate = 300000) // Runs every 5 minutes (300,000 ms)
    public void updateComplianceChecks() {
        logger.info("Starting automated compliance check update at {}", LocalDateTime.now());
        List<ComplianceCheck> allChecks = checkService.findAll();

        if (allChecks.isEmpty()) {
            logger.info("No compliance checks found to update.");
            return;
        }

        Random random = new Random();
        for (ComplianceCheck check : allChecks) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastChecked = check.getCheckedAt();

            if (lastChecked == null || lastChecked.isBefore(now.minusHours(24))) {
                // Check hasn't been updated in 24 hours, set to PENDING
                check.setStatus(CheckStatus.PENDING);
            } else {
                // Randomly assign COMPLIANT or NON_COMPLIANT (for demo purposes)
                check.setStatus(random.nextBoolean() ? CheckStatus.COMPLIANT : CheckStatus.NON_COMPLIANT);
            }
            check.setCheckedAt(now);
            checkService.updateCheck(check.getId(), check);
            logger.debug("Updated check ID {} to status {}", check.getId(), check.getStatus());
        }
        logger.info("Completed automated compliance check update.");
    }
}