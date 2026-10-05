package com.chubb.claims.claim.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignClaimRequest(

        @NotBlank(message = "Officer ID is required")
        String officerId,

        @NotBlank(message = "Assigned by is required")
        String assignedBy
) {
}