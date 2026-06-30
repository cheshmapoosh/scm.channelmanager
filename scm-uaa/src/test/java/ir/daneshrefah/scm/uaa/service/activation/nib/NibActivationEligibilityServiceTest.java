package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_ACTIVATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NibActivationEligibilityServiceTest {
    private final NibActivationUserLookup userLookup = mock(NibActivationUserLookup.class);
    private final NibActivationEligibilityService service = new NibActivationEligibilityService(userLookup);

    @Test
    void rejectsRequestWithoutActivationScope() {
        ActivationCandidateRequest request = new ActivationCandidateRequest(
                "user",
                Set.of("openid"),
                TerminalType.IB,
                TerminalType.NIB
        );

        assertEquals(NibActivationEligibilityService.CandidateStatus.REJECTED, service.checkCandidate(request));
    }

    @Test
    void rejectsInvalidSourceOrNonNibTarget() {
        assertEquals(
                NibActivationEligibilityService.CandidateStatus.HAS_ERROR,
                service.checkCandidate(new ActivationCandidateRequest(
                        "user", Set.of(OAUTH2_SCOPE_NAME_ACTIVATION), null, TerminalType.NIB
                ))
        );
        assertEquals(
                NibActivationEligibilityService.CandidateStatus.HAS_ERROR,
                service.checkCandidate(new ActivationCandidateRequest(
                        "user", Set.of(OAUTH2_SCOPE_NAME_ACTIVATION), TerminalType.IB, TerminalType.MB
                ))
        );
    }

    @Test
    void usesRequestedNibTargetWhenCheckingExistingActivation() {
        ActivationCandidateRequest request = activationRequest();
        when(userLookup.existsUser("user", TerminalType.NIB)).thenReturn(true);

        assertEquals(
                NibActivationEligibilityService.AuthenticationStatus.ACTIVATED_BEFORE,
                service.checkAuthentication(request)
        );
        verify(userLookup).existsUser("user", TerminalType.NIB);
    }

    @Test
    void acceptsExistingSourceUserWhenNibDoesNotExist() {
        ActivationCandidateRequest request = activationRequest();
        when(userLookup.existsUser("user", TerminalType.NIB)).thenReturn(false);
        when(userLookup.existsUser("user", TerminalType.IB)).thenReturn(true);

        assertEquals(
                NibActivationEligibilityService.AuthenticationStatus.READY_FOR_AUTHENTICATE,
                service.checkAuthentication(request)
        );
    }

    @Test
    void rejectsNonNibAuthenticationTarget() {
        ActivationCandidateRequest request = new ActivationCandidateRequest(
                "user", Set.of(OAUTH2_SCOPE_NAME_ACTIVATION), TerminalType.IB, TerminalType.MB
        );

        assertThrows(IllegalArgumentException.class, () -> service.checkAuthentication(request));
    }

    private ActivationCandidateRequest activationRequest() {
        return new ActivationCandidateRequest(
                "user",
                Set.of(OAUTH2_SCOPE_NAME_ACTIVATION),
                TerminalType.IB,
                TerminalType.NIB
        );
    }
}
