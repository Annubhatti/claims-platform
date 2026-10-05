package com.chubb.claims.common.exception;

import java.util.UUID;

public class ClaimNotFoundException extends RuntimeException {

    public ClaimNotFoundException(UUID claimId) {
        super("Claim not found: " + claimId);
    }
}