package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record ApproveClaimRequest(
        @NotBlank(message = "Approved by is required")
        String approvedBy,

        String reason
) {
}