package com.chubb.claims.claim.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "claim_assessment")
public class ClaimAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false, unique = true)
    private UUID claimId;

    @Column(name = "estimated_liability", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedLiability;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "assessment_notes", columnDefinition = "TEXT")
    private String assessmentNotes;

    @Column(name = "assessed_by", nullable = false, length = 100)
    private String assessedBy;

    @Column(name = "assessed_at", nullable = false)
    private OffsetDateTime assessedAt;

    public ClaimAssessment() {
    }

    public ClaimAssessment(
            UUID claimId,
            BigDecimal estimatedLiability,
            BigDecimal approvedAmount,
            String assessmentNotes,
            String assessedBy
    ) {
        this.claimId = claimId;
        this.estimatedLiability = estimatedLiability;
        this.approvedAmount = approvedAmount;
        this.assessmentNotes = assessmentNotes;
        this.assessedBy = assessedBy;
        this.assessedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public BigDecimal getEstimatedLiability() {
        return estimatedLiability;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public String getAssessmentNotes() {
        return assessmentNotes;
    }

    public String getAssessedBy() {
        return assessedBy;
    }

    public OffsetDateTime getAssessedAt() {
        return assessedAt;
    }
}