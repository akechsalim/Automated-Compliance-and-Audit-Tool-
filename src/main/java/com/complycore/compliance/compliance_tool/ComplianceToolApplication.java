package com.complycore.compliance.compliance_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComplianceToolApplication {
	public static void main(String[] args) {
		SpringApplication.run(ComplianceToolApplication.class, args);
	}
}
