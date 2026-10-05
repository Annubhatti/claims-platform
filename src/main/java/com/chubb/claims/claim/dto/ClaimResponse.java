package com.chubb.claims.claim.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.chubb.claims.claim.entity.ClaimStatus;

public record ClaimResponse(

        UUID id,

        String claimNumber,

        String claimantId,

        String market,

        String claimType,

        String description,

        ClaimStatus status,

        BigDecimal estimatedLiability,

        BigDecimal approvedAmount,

        BigDecimal settlementAmount,

        String assignedOfficerId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}