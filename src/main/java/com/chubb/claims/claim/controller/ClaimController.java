package com.chubb.claims.claim.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chubb.claims.claim.dto.ApproveClaimRequest;
import com.chubb.claims.claim.dto.AssessmentRequest;
import com.chubb.claims.claim.dto.AssignClaimRequest;
import com.chubb.claims.claim.dto.ClaimResponse;
import com.chubb.claims.claim.dto.CreateClaimRequest;
import com.chubb.claims.claim.dto.ProvideInformationRequest;
import com.chubb.claims.claim.dto.RejectClaimRequest;
import com.chubb.claims.claim.dto.RequestInformationRequest;
import com.chubb.claims.claim.dto.ReviewClaimRequest;
import com.chubb.claims.claim.dto.SettleClaimRequest;
import com.chubb.claims.claim.entity.ClaimHistory;
import com.chubb.claims.claim.entity.ClaimInformation;
import com.chubb.claims.claim.entity.ClaimStatus;
import com.chubb.claims.claim.service.ClaimService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    // =========================================================
    // CREATE CLAIM
    // POST /api/v1/claims
    // =========================================================

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request
    ) {

        ClaimResponse response =
                claimService.createClaim(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // GET CLAIMS BY STATUS
    //
    // GET /api/v1/claims?status=SUBMITTED
    //
    // If status is not provided, default to SUBMITTED.
    // =========================================================

    @GetMapping
public ResponseEntity<Page<ClaimResponse>> getClaims(
        @RequestParam(required = false) ClaimStatus status,
        Pageable pageable
) {

    if (status == null) {
        status = ClaimStatus.SUBMITTED;
    }

    return ResponseEntity.ok(
            claimService.getClaimsByStatus(
                    status,
                    pageable
            )
    );
}

    // =========================================================
    // GET CLAIMS BY OFFICER
    //
    // GET /api/v1/claims/officer/OFFICER-101
    //
    // Optional status:
    //
    // GET /api/v1/claims/officer/OFFICER-101?status=UNDER_REVIEW
    // =========================================================

    @GetMapping("/officer/{officerId}")
public ResponseEntity<Page<ClaimResponse>> getClaimsByOfficer(
        @PathVariable String officerId,
        @RequestParam(required = false) ClaimStatus status,
        Pageable pageable
) {

    return ResponseEntity.ok(
            claimService.getClaimsByOfficer(
                    officerId,
                    status,
                    pageable
            )
    );
}

    // =========================================================
    // GET SINGLE CLAIM
    //
    // GET /api/v1/claims/{claimId}
    // =========================================================

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaim(
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getClaim(claimId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ASSIGN CLAIM
    //
    // SUBMITTED -> UNDER_REVIEW
    //
    // POST /api/v1/claims/{claimId}/assign
    // =========================================================

    @PostMapping("/{claimId}/assign")
    public ResponseEntity<ClaimResponse> assignClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody AssignClaimRequest request
    ) {

        ClaimResponse response =
                claimService.assignClaim(
                        claimId,
                        request.officerId(),
                        request.assignedBy()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // REVIEW CLAIM
    //
    // POST /api/v1/claims/{claimId}/review
    // =========================================================

    @PostMapping("/{claimId}/review")
    public ResponseEntity<ClaimResponse> reviewClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody ReviewClaimRequest request
    ) {

        ClaimResponse response =
                claimService.reviewClaim(
                        claimId,
                        request.reviewedBy()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // REQUEST ADDITIONAL INFORMATION
    //
    // UNDER_REVIEW -> AWAITING_INFORMATION
    //
    // POST /api/v1/claims/{claimId}/request-information
    // =========================================================

    @PostMapping("/{claimId}/request-information")
    public ResponseEntity<ClaimResponse> requestInformation(
            @PathVariable UUID claimId,
            @Valid @RequestBody RequestInformationRequest request
    ) {

        ClaimResponse response =
                claimService.requestInformation(
                        claimId,
                        request.requestedBy(),
                        request.reason()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // PROVIDE ADDITIONAL INFORMATION
    //
    // AWAITING_INFORMATION -> UNDER_REVIEW
    //
    // POST /api/v1/claims/{claimId}/information
    // =========================================================

    @PostMapping("/{claimId}/information")
    public ResponseEntity<ClaimResponse> provideInformation(
            @PathVariable UUID claimId,
            @Valid @RequestBody ProvideInformationRequest request
    ) {

        ClaimResponse response =
                claimService.provideInformation(
                        claimId,
                        request.providedBy(),
                        request.information()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ASSESS CLAIM
    //
    // UNDER_REVIEW -> ASSESSED
    //
    // POST /api/v1/claims/{claimId}/assessment
    // =========================================================

    @PostMapping("/{claimId}/assessment")
    public ResponseEntity<ClaimResponse> assessClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody AssessmentRequest request
    ) {

        ClaimResponse response =
                claimService.assessClaim(
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // APPROVE CLAIM
    //
    // ASSESSED -> APPROVED
    //
    // POST /api/v1/claims/{claimId}/approve
    // =========================================================

    @PostMapping("/{claimId}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody ApproveClaimRequest request
    ) {

        ClaimResponse response =
                claimService.approveClaim(
                        claimId,
                        request.approvedBy(),
                        request.reason()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // REJECT CLAIM
    //
    // ASSESSED -> REJECTED
    //
    // POST /api/v1/claims/{claimId}/reject
    // =========================================================

    @PostMapping("/{claimId}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody RejectClaimRequest request
    ) {

        ClaimResponse response =
                claimService.rejectClaim(
                        claimId,
                        request.rejectedBy(),
                        request.reason()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // SETTLE CLAIM
    //
    // APPROVED -> SETTLED
    //
    // POST /api/v1/claims/{claimId}/settle
    // =========================================================

    @PostMapping("/{claimId}/settle")
    public ResponseEntity<ClaimResponse> settleClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody SettleClaimRequest request
    ) {

        ClaimResponse response =
                claimService.settleClaim(
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CLAIM HISTORY
    //
    // GET /api/v1/claims/{claimId}/history
    // =========================================================

    @GetMapping("/{claimId}/history")
    public ResponseEntity<List<ClaimHistory>> getClaimHistory(
            @PathVariable UUID claimId
    ) {

        return ResponseEntity.ok(
                claimService.getClaimHistory(claimId)
        );
    }

    // =========================================================
    // CLAIM INFORMATION
    //
    // GET /api/v1/claims/{claimId}/information
    // =========================================================

    @GetMapping("/{claimId}/information")
    public ResponseEntity<List<ClaimInformation>> getClaimInformation(
            @PathVariable UUID claimId
    ) {

        return ResponseEntity.ok(
                claimService.getClaimInformation(claimId)
        );
    }
}