package com.complycore.compliance.compliance_tool.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "compliance_checks")
public class ComplianceCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    @JsonBackReference
    private ComplianceRule rule;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(name = "checked_at")
    private java.time.LocalDateTime checkedAt;

}

