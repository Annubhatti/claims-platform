package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record ProvideInformationRequest(
        @NotBlank(message = "Provided by is required")
        String providedBy,

        @NotBlank(message = "Information is required")
        String information
) {
}