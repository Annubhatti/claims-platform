package com.chubb.claims.claim.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssessmentRequest(

        @NotNull(message = "Estimated liability is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal estimatedLiability,

        @NotNull(message = "Approved amount is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal approvedAmount,

        @NotBlank(message = "Assessment notes are required")
        String assessmentNotes,

        @NotBlank(message = "Assessed by is required")
        String assessedBy
) {
}