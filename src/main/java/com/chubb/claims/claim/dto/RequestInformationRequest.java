package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestInformationRequest(
        @NotBlank(message = "Requested by is required")
        String requestedBy,

        @NotBlank(message = "Reason is required")
        String reason
) {
}