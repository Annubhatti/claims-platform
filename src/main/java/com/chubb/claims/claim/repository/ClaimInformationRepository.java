package com.chubb.claims.claim.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chubb.claims.claim.entity.ClaimInformation;

public interface ClaimInformationRepository
        extends JpaRepository<ClaimInformation, UUID> {

    List<ClaimInformation> findByClaimIdOrderByCreatedAtAsc(UUID claimId);
}