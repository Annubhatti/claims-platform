package com.chubb.claims.claim.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClaimRequest(

        @NotBlank(message = "Claimant ID is required")
        String claimantId,

        @NotBlank(message = "Market is required")
        String market,

        @NotBlank(message = "Claim type is required")
        String claimType,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Estimated liability is required")
        @DecimalMin(value = "0.0", inclusive = true,
                message = "Estimated liability cannot be negative")
        BigDecimal estimatedLiability
) {
}