package com.chubb.claims.claim.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SettleClaimRequest(

        @NotNull(message = "Settlement amount is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal settlementAmount,

        @NotBlank(message = "Settled by is required")
        String settledBy
) {
}