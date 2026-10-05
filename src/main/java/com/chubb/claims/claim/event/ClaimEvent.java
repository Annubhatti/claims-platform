package com.chubb.claims.claim.event;

import com.chubb.claims.claim.entity.ClaimStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClaimEvent(
        UUID claimId,
        String claimNumber,
        ClaimEventType eventType,
        ClaimStatus previousStatus,
        ClaimStatus currentStatus,
        BigDecimal estimatedLiability,
        BigDecimal approvedAmount,
        BigDecimal settlementAmount,
        String performedBy,
        OffsetDateTime occurredAt
) {
}