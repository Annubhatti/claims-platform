package com.chubb.claims.dashboard.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chubb.claims.claim.entity.ClaimStatus;
import com.chubb.claims.claim.repository.ClaimRepository;
import com.chubb.claims.dashboard.dto.ExposureResponse;
import com.chubb.claims.dashboard.dto.WorkloadResponse;

@Service
public class DashboardService {

    private final ClaimRepository claimRepository;

    public DashboardService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public WorkloadResponse getWorkload() {

        Map<String, Long> claimsByStatus =
        new java.util.LinkedHashMap<>();

        for (ClaimStatus status : ClaimStatus.values()) {
            claimsByStatus.put(
                    status.name(),
                    claimRepository.countByStatus(status)
            );
        }

        long totalClaims = claimRepository.count();

        return new WorkloadResponse(
                totalClaims,
                claimsByStatus
        );
    }

    @Transactional(readOnly = true)
    public ExposureResponse getExposure() {

        BigDecimal outstandingLiability =
                claimRepository.calculateOutstandingLiability();

        return new ExposureResponse(
                outstandingLiability
        );
    }
}