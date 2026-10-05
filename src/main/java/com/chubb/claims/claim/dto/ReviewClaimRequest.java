package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewClaimRequest(
        @NotBlank(message = "Reviewed by is required")
        String reviewedBy
) {
}