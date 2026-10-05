package com.chubb.claims.dashboard.dto;

import java.math.BigDecimal;

public record ExposureResponse(
        BigDecimal outstandingLiability
) {
}