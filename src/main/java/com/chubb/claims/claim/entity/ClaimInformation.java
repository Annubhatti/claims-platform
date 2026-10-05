package com.chubb.claims.claim.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "claim_information",
        indexes = {
                @Index(name = "idx_claim_information_claim", columnList = "claim_id")
        }
)
public class ClaimInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "information", nullable = false, columnDefinition = "TEXT")
    private String information;

    @Column(name = "provided_by", nullable = false, length = 100)
    private String providedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ClaimInformation() {
    }

    public ClaimInformation(
            UUID claimId,
            String information,
            String providedBy
    ) {
        this.claimId = claimId;
        this.information = information;
        this.providedBy = providedBy;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public String getInformation() {
        return information;
    }

    public String getProvidedBy() {
        return providedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}