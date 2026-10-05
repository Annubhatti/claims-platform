package com.chubb.claims.dashboard.dto;

import java.util.Map;

public record WorkloadResponse(
        long totalClaims,
        Map<String, Long> claimsByStatus
) {
}