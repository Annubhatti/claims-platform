package com.chubb.claims.claim.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.chubb.claims.claim.entity.ClaimStatus;

public record ClaimCreatedEvent(

        UUID claimId,

        String claimNumber,

        String claimantId,

        String market,

        ClaimStatus status,

        BigDecimal estimatedLiability,

        OffsetDateTime createdAt
) {
}