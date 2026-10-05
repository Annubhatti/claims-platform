package com.chubb.claims.claim.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chubb.claims.claim.dto.AssessmentRequest;
import com.chubb.claims.claim.dto.ClaimResponse;
import com.chubb.claims.claim.dto.CreateClaimRequest;
import com.chubb.claims.claim.dto.SettleClaimRequest;
import com.chubb.claims.claim.entity.Claim;
import com.chubb.claims.claim.entity.ClaimAssessment;
import com.chubb.claims.claim.entity.ClaimHistory;
import com.chubb.claims.claim.entity.ClaimInformation;
import com.chubb.claims.claim.entity.ClaimStatus;
import com.chubb.claims.claim.event.ClaimEvent;
import com.chubb.claims.claim.event.ClaimEventPublisher;
import com.chubb.claims.claim.event.ClaimEventType;
import com.chubb.claims.claim.repository.ClaimAssessmentRepository;
import com.chubb.claims.claim.repository.ClaimHistoryRepository;
import com.chubb.claims.claim.repository.ClaimInformationRepository;
import com.chubb.claims.claim.repository.ClaimRepository;
import com.chubb.claims.common.exception.ClaimNotFoundException;
import com.chubb.claims.common.exception.InvalidClaimStateException;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimEventPublisher claimEventPublisher;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final ClaimInformationRepository claimInformationRepository;
    private final ClaimAssessmentRepository claimAssessmentRepository;

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimEventPublisher claimEventPublisher,
            ClaimHistoryRepository claimHistoryRepository,
            ClaimInformationRepository claimInformationRepository,
            ClaimAssessmentRepository claimAssessmentRepository
    ) {
        this.claimRepository = claimRepository;
        this.claimEventPublisher = claimEventPublisher;
        this.claimHistoryRepository = claimHistoryRepository;
        this.claimInformationRepository = claimInformationRepository;
        this.claimAssessmentRepository = claimAssessmentRepository;
    }

    // =========================================================
    // CREATE CLAIM
    // =========================================================

    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request) {

        Claim claim = new Claim();

        claim.setClaimNumber(generateClaimNumber());
        claim.setClaimantId(request.claimantId());
        claim.setMarket(request.market());
        claim.setClaimType(request.claimType());
        claim.setDescription(request.description());
        claim.setEstimatedLiability(request.estimatedLiability());
        claim.setStatus(ClaimStatus.SUBMITTED);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                null,
                ClaimStatus.SUBMITTED,
                request.claimantId(),
                "Claim submitted"
        );

        publishEvent(
                savedClaim,
                null,
                ClaimStatus.SUBMITTED,
                ClaimEventType.CLAIM_CREATED,
                request.claimantId()
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // GET CLAIM
    // =========================================================

    @Transactional(readOnly = true)
    public ClaimResponse getClaim(UUID claimId) {

        Claim claim = findClaim(claimId);

        return toResponse(claim);
    }

    // =========================================================
    // ASSIGN CLAIM
    // SUBMITTED -> UNDER_REVIEW
    // =========================================================

    @Transactional
    public ClaimResponse assignClaim(
            UUID claimId,
            String officerId,
            String assignedBy
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.SUBMITTED,
                "Only SUBMITTED claims can be assigned"
        );

        ClaimStatus previousStatus = claim.getStatus();

        claim.setAssignedOfficerId(officerId);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.UNDER_REVIEW,
                assignedBy,
                "Claim assigned to officer " + officerId
        );

        publishEvent(
                savedClaim,
                previousStatus,
                ClaimStatus.UNDER_REVIEW,
                ClaimEventType.CLAIM_ASSIGNED,
                assignedBy
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // REVIEW CLAIM
    // UNDER_REVIEW -> UNDER_REVIEW
    // =========================================================

    @Transactional
    public ClaimResponse reviewClaim(
            UUID claimId,
            String reviewedBy
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.UNDER_REVIEW,
                "Only UNDER_REVIEW claims can be reviewed"
        );

        saveHistory(
                claim,
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.UNDER_REVIEW,
                reviewedBy,
                "Claim reviewed"
        );

        return toResponse(claim);
    }

    // =========================================================
    // REQUEST INFORMATION
    // UNDER_REVIEW -> AWAITING_INFORMATION
    // =========================================================

    @Transactional
    public ClaimResponse requestInformation(
            UUID claimId,
            String requestedBy,
            String reason
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.UNDER_REVIEW,
                "Information can only be requested for claims under review"
        );

        ClaimStatus previousStatus = claim.getStatus();

        claim.setStatus(ClaimStatus.AWAITING_INFORMATION);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.AWAITING_INFORMATION,
                requestedBy,
                reason
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // PROVIDE INFORMATION
    // AWAITING_INFORMATION -> UNDER_REVIEW
    // =========================================================

    @Transactional
    public ClaimResponse provideInformation(
            UUID claimId,
            String providedBy,
            String information
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.AWAITING_INFORMATION,
                "Information can only be provided when the claim is awaiting information"
        );

        ClaimInformation claimInformation =
                new ClaimInformation(
                        claimId,
                        information,
                        providedBy
                );

        claimInformationRepository.save(claimInformation);

        ClaimStatus previousStatus = claim.getStatus();

        claim.setStatus(ClaimStatus.UNDER_REVIEW);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.UNDER_REVIEW,
                providedBy,
                "Additional claim information provided"
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // ASSESS CLAIM
    // UNDER_REVIEW -> ASSESSED
    // =========================================================

    @Transactional
    public ClaimResponse assessClaim(
            UUID claimId,
            AssessmentRequest request
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.UNDER_REVIEW,
                "Only UNDER_REVIEW claims can be assessed"
        );

        if (request.approvedAmount()
                .compareTo(request.estimatedLiability()) > 0) {

            throw new IllegalArgumentException(
                    "Approved amount cannot exceed estimated liability"
            );
        }

        ClaimAssessment assessment =
                new ClaimAssessment(
                        claimId,
                        request.estimatedLiability(),
                        request.approvedAmount(),
                        request.assessmentNotes(),
                        request.assessedBy()
                );

        claimAssessmentRepository.save(assessment);

        ClaimStatus previousStatus = claim.getStatus();

        claim.setEstimatedLiability(request.estimatedLiability());
        claim.setApprovedAmount(request.approvedAmount());
        claim.setStatus(ClaimStatus.ASSESSED);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.ASSESSED,
                request.assessedBy(),
                "Claim assessed"
        );

        publishEvent(
                savedClaim,
                previousStatus,
                ClaimStatus.ASSESSED,
                ClaimEventType.CLAIM_ASSESSED,
                request.assessedBy()
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // APPROVE CLAIM
    // ASSESSED -> APPROVED
    // =========================================================

    @Transactional
    public ClaimResponse approveClaim(
            UUID claimId,
            String approvedBy,
            String reason
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.ASSESSED,
                "Only ASSESSED claims can be approved"
        );

       if (claim.getApprovedAmount() == null) {
    throw new InvalidClaimStateException(
            "Claim cannot be approved without an approved amount"
    );
}

        ClaimStatus previousStatus = claim.getStatus();

        claim.setStatus(ClaimStatus.APPROVED);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.APPROVED,
                approvedBy,
                reason != null ? reason : "Claim approved"
        );

        publishEvent(
                savedClaim,
                previousStatus,
                ClaimStatus.APPROVED,
                ClaimEventType.CLAIM_APPROVED,
                approvedBy
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // REJECT CLAIM
    // ASSESSED -> REJECTED
    // =========================================================

    @Transactional
    public ClaimResponse rejectClaim(
            UUID claimId,
            String rejectedBy,
            String reason
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.ASSESSED,
                "Only ASSESSED claims can be rejected"
        );

        ClaimStatus previousStatus = claim.getStatus();

        claim.setStatus(ClaimStatus.REJECTED);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.REJECTED,
                rejectedBy,
                reason
        );

        publishEvent(
                savedClaim,
                previousStatus,
                ClaimStatus.REJECTED,
                ClaimEventType.CLAIM_REJECTED,
                rejectedBy
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // SETTLE CLAIM
    // APPROVED -> SETTLED
    // =========================================================

    @Transactional
    public ClaimResponse settleClaim(
            UUID claimId,
            SettleClaimRequest request
    ) {

        Claim claim = findClaim(claimId);

        validateStatus(
                claim,
                ClaimStatus.APPROVED,
                "Only APPROVED claims can be settled"
        );

        if (request.settlementAmount()
                .compareTo(claim.getApprovedAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Settlement amount cannot exceed approved amount"
            );
        }

        ClaimStatus previousStatus = claim.getStatus();

        claim.setSettlementAmount(request.settlementAmount());
        claim.setStatus(ClaimStatus.SETTLED);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                previousStatus,
                ClaimStatus.SETTLED,
                request.settledBy(),
                "Claim settled"
        );

        publishEvent(
                savedClaim,
                previousStatus,
                ClaimStatus.SETTLED,
                ClaimEventType.CLAIM_SETTLED,
                request.settledBy()
        );

        return toResponse(savedClaim);
    }

    // =========================================================
    // GET CLAIM HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<ClaimHistory> getClaimHistory(UUID claimId) {

        findClaim(claimId);

        return claimHistoryRepository
                .findByClaimIdOrderByChangedAtAsc(claimId);
    }

    // =========================================================
    // GET CLAIM INFORMATION
    // =========================================================

    @Transactional(readOnly = true)
    public List<ClaimInformation> getClaimInformation(UUID claimId) {

        findClaim(claimId);

        return claimInformationRepository
                .findByClaimIdOrderByCreatedAtAsc(claimId);
    }


    @Transactional(readOnly = true)
public Page<ClaimResponse> getClaimsByStatus(
        ClaimStatus status,
        Pageable pageable
) {
    return claimRepository
            .findByStatus(status, pageable)
            .map(this::toResponse);
}

@Transactional(readOnly = true)
public Page<ClaimResponse> getClaimsByOfficer(
        String officerId,
        ClaimStatus status,
        Pageable pageable
) {

    if (status != null) {
        return claimRepository
                .findByAssignedOfficerIdAndStatus(
                        officerId,
                        status,
                        pageable
                )
                .map(this::toResponse);
    }

    return claimRepository
            .findByAssignedOfficerId(
                    officerId,
                    pageable
            )
            .map(this::toResponse);
}

    // =========================================================
    // PRIVATE HELPER METHODS
    // =========================================================

    private Claim findClaim(UUID claimId) {

        return claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ClaimNotFoundException(claimId)
                );
    }

    private void validateStatus(
            Claim claim,
            ClaimStatus expectedStatus,
            String message
    ) {

        if (claim.getStatus() != expectedStatus) {
            throw new InvalidClaimStateException(message);
        }
    }

    private void saveHistory(
            Claim claim,
            ClaimStatus fromStatus,
            ClaimStatus toStatus,
            String changedBy,
            String reason
    ) {

        ClaimHistory history =
                new ClaimHistory(
                        claim.getId(),
                        fromStatus,
                        toStatus,
                        changedBy,
                        reason
                );

        claimHistoryRepository.save(history);
    }

    private void publishEvent(
            Claim claim,
            ClaimStatus previousStatus,
            ClaimStatus currentStatus,
            ClaimEventType eventType,
            String performedBy
    ) {

        ClaimEvent event = new ClaimEvent(
                claim.getId(),
                claim.getClaimNumber(),
                eventType,
                previousStatus,
                currentStatus,
                claim.getEstimatedLiability(),
                claim.getApprovedAmount(),
                claim.getSettlementAmount(),
                performedBy,
                OffsetDateTime.now()
        );

        claimEventPublisher.publish(event);
    }

    private String generateClaimNumber() {

        return "CLM-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private ClaimResponse toResponse(Claim claim) {

        return new ClaimResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getClaimantId(),
                claim.getMarket(),
                claim.getClaimType(),
                claim.getDescription(),
                claim.getStatus(),
                claim.getEstimatedLiability(),
                claim.getApprovedAmount(),
                claim.getSettlementAmount(),
                claim.getAssignedOfficerId(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}