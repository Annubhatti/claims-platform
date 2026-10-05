package com.chubb.claims.claim.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.chubb.claims.claim.entity.Claim;
import com.chubb.claims.claim.entity.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    boolean existsByClaimNumber(String claimNumber);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByAssignedOfficerIdAndStatus(
            String assignedOfficerId,
            ClaimStatus status
    );

    Page<Claim> findByStatus(
            ClaimStatus status,
            Pageable pageable
    );

    Page<Claim> findByAssignedOfficerId(
            String assignedOfficerId,
            Pageable pageable
    );

    Page<Claim> findByAssignedOfficerIdAndStatus(
            String assignedOfficerId,
            ClaimStatus status,
            Pageable pageable
    );

    long countByStatus(ClaimStatus status);

    @Query("""
            SELECT COALESCE(SUM(c.estimatedLiability), 0)
            FROM Claim c
            WHERE c.status NOT IN (
                com.chubb.claims.claim.entity.ClaimStatus.REJECTED,
                com.chubb.claims.claim.entity.ClaimStatus.SETTLED
            )
            """)
    BigDecimal calculateOutstandingLiability();
}