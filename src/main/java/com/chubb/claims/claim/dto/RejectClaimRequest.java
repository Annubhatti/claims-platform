package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectClaimRequest(
        @NotBlank(message = "Rejected by is required")
        String rejectedBy,

        @NotBlank(message = "Rejection reason is required")
        String reason
) {
}