package com.chubb.claims.claim.service;

import com.chubb.claims.claim.dto.AssessmentRequest;
import com.chubb.claims.claim.dto.CreateClaimRequest;
import com.chubb.claims.claim.dto.SettleClaimRequest;
import com.chubb.claims.claim.entity.Claim;
import com.chubb.claims.claim.entity.ClaimStatus;
import com.chubb.claims.claim.event.ClaimEventPublisher;
import com.chubb.claims.claim.event.ClaimEventType;
import com.chubb.claims.claim.repository.ClaimAssessmentRepository;
import com.chubb.claims.claim.repository.ClaimHistoryRepository;
import com.chubb.claims.claim.repository.ClaimInformationRepository;
import com.chubb.claims.claim.repository.ClaimRepository;
import com.chubb.claims.common.exception.InvalidClaimStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @Mock
    private ClaimHistoryRepository claimHistoryRepository;

    @Mock
    private ClaimInformationRepository claimInformationRepository;

    @Mock
    private ClaimAssessmentRepository claimAssessmentRepository;

    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(
                claimRepository,
                claimEventPublisher,
                claimHistoryRepository,
                claimInformationRepository,
                claimAssessmentRepository
        );
    }

    @Test
    void shouldCreateClaimWithSubmittedStatus() {

        CreateClaimRequest request = new CreateClaimRequest(
                "CUSTOMER-1001",
                "SG",
                "MOTOR",
                "Vehicle accident",
                new BigDecimal("20000")
        );

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> {
                    Claim claim = invocation.getArgument(0);
                    return claim;
                });

        var response = claimService.createClaim(request);

        assertNotNull(response);
        assertEquals("CUSTOMER-1001", response.claimantId());
        assertEquals("SG", response.market());
        assertEquals("MOTOR", response.claimType());
        assertEquals(ClaimStatus.SUBMITTED, response.status());
        assertEquals(
                new BigDecimal("20000"),
                response.estimatedLiability()
        );

        verify(claimRepository).save(any(Claim.class));
        verify(claimHistoryRepository).save(any());
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldAssignSubmittedClaim() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.SUBMITTED
        );

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = claimService.assignClaim(
                claimId,
                "OFFICER-101",
                "MANAGER-001"
        );

        assertEquals(
                ClaimStatus.UNDER_REVIEW,
                response.status()
        );

        assertEquals(
                "OFFICER-101",
                response.assignedOfficerId()
        );

        verify(claimEventPublisher).publish(argThat(
                event -> event.eventType() == ClaimEventType.CLAIM_ASSIGNED
        ));
    }

    @Test
    void shouldRejectInvalidAssignmentState() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.ASSESSED
        );

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        assertThrows(
                InvalidClaimStateException.class,
                () -> claimService.assignClaim(
                        claimId,
                        "OFFICER-101",
                        "MANAGER-001"
                )
        );

        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any());
    }

    @Test
    void shouldAssessClaim() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.UNDER_REVIEW
        );

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentRequest request = new AssessmentRequest(
                new BigDecimal("20000"),
                new BigDecimal("18000"),
                "Damage verified",
                "OFFICER-101"
        );

        var response = claimService.assessClaim(
                claimId,
                request
        );

        assertEquals(
                ClaimStatus.ASSESSED,
                response.status()
        );

        assertEquals(
                new BigDecimal("18000"),
                response.approvedAmount()
        );

        verify(claimAssessmentRepository).save(any());

        verify(claimEventPublisher).publish(argThat(
                event -> event.eventType() == ClaimEventType.CLAIM_ASSESSED
        ));
    }

    @Test
    void shouldRejectAssessmentWhenApprovedAmountExceedsLiability() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.UNDER_REVIEW
        );

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        AssessmentRequest request = new AssessmentRequest(
                new BigDecimal("20000"),
                new BigDecimal("25000"),
                "Invalid assessment",
                "OFFICER-101"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> claimService.assessClaim(
                        claimId,
                        request
                )
        );

        verify(claimAssessmentRepository, never()).save(any());
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any());
    }

    @Test
    void shouldApproveAssessedClaim() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.ASSESSED
        );

        claim.setApprovedAmount(new BigDecimal("18000"));

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = claimService.approveClaim(
                claimId,
                "MANAGER-001",
                "Approved"
        );

        assertEquals(
                ClaimStatus.APPROVED,
                response.status()
        );

        verify(claimEventPublisher).publish(argThat(
                event -> event.eventType() == ClaimEventType.CLAIM_APPROVED
        ));
    }

    @Test
    void shouldSettleApprovedClaim() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.APPROVED
        );

        claim.setApprovedAmount(new BigDecimal("18000"));

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SettleClaimRequest request = new SettleClaimRequest(
                new BigDecimal("18000"),
                "OFFICER-101"
        );

        var response = claimService.settleClaim(
                claimId,
                request
        );

        assertEquals(
                ClaimStatus.SETTLED,
                response.status()
        );

        assertEquals(
                new BigDecimal("18000"),
                response.settlementAmount()
        );

        verify(claimEventPublisher).publish(argThat(
                event -> event.eventType() == ClaimEventType.CLAIM_SETTLED
        ));
    }

    @Test
    void shouldRejectSettlementWhenAmountExceedsApprovedAmount() {

        UUID claimId = UUID.randomUUID();

        Claim claim = createClaim(
                claimId,
                ClaimStatus.APPROVED
        );

        claim.setApprovedAmount(new BigDecimal("18000"));

        when(claimRepository.findById(claimId))
                .thenReturn(java.util.Optional.of(claim));

        SettleClaimRequest request = new SettleClaimRequest(
                new BigDecimal("20000"),
                "OFFICER-101"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> claimService.settleClaim(
                        claimId,
                        request
                )
        );

        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any());
    }

    private Claim createClaim(
            UUID claimId,
            ClaimStatus status
    ) {
        Claim claim = new Claim();

        try {
            var idField = Claim.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(claim, claimId);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        claim.setClaimNumber("CLM-TEST-001");
        claim.setClaimantId("CUSTOMER-1001");
        claim.setMarket("SG");
        claim.setClaimType("MOTOR");
        claim.setDescription("Test claim");
        claim.setEstimatedLiability(new BigDecimal("20000"));
        claim.setStatus(status);

        return claim;
    }
}
