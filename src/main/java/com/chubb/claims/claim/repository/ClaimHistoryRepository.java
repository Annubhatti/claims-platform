package com.chubb.claims.claim.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chubb.claims.claim.entity.ClaimHistory;

public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, UUID> {

    List<ClaimHistory> findByClaimIdOrderByChangedAtAsc(UUID claimId);
}