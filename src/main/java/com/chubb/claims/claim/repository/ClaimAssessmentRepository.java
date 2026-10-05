package com.chubb.claims.claim.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chubb.claims.claim.entity.ClaimAssessment;

public interface ClaimAssessmentRepository
        extends JpaRepository<ClaimAssessment, UUID> {

    Optional<ClaimAssessment> findByClaimId(UUID claimId);
}