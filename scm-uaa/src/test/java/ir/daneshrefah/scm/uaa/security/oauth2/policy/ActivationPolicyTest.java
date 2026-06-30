package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.nib.ActivationCandidateRequest;
import ir.daneshrefah.scm.uaa.service.activation.nib.NibActivationEligibilityService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_ACTIVATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivationPolicyTest {

    @Test
    void mapsSecurityTokenToBusinessRequestAtPolicyBoundary() {
        NibActivationEligibilityService eligibilityService = mock(NibActivationEligibilityService.class);
        ObjectProvider<NibActivationEligibilityService> provider = provider(eligibilityService);
        PreAuthenticationToken authentication = mock(PreAuthenticationToken.class);
        when(authentication.getName()).thenReturn("user");
        when(authentication.getScopes()).thenReturn(Set.of(OAUTH2_SCOPE_NAME_ACTIVATION));
        when(authentication.getActivatorTerminal()).thenReturn(TerminalType.IB.name());
        when(eligibilityService.checkCandidate(any()))
                .thenReturn(NibActivationEligibilityService.CandidateStatus.REJECTED);

        new ActivationPolicy(provider).decide(authentication, TerminalType.NIB.name());

        ArgumentCaptor<ActivationCandidateRequest> request =
                ArgumentCaptor.forClass(ActivationCandidateRequest.class);
        verify(eligibilityService).checkCandidate(request.capture());
        assertEquals("user", request.getValue().username());
        assertEquals(Set.of(OAUTH2_SCOPE_NAME_ACTIVATION), request.getValue().scopes());
        assertEquals(TerminalType.IB, request.getValue().fromTerminal());
        assertEquals(TerminalType.NIB, request.getValue().toTerminal());
    }

    @Test
    void remainsUsableWhenNibActivationIsDisabled() {
        ObjectProvider<NibActivationEligibilityService> provider = provider(null);
        PreAuthenticationToken authentication = mock(PreAuthenticationToken.class);
        when(authentication.getName()).thenReturn("user");

        ActivationPolicy.ActivationDecision decision =
                new ActivationPolicy(provider).decide(authentication, TerminalType.IB.name());

        assertTrue(decision.cacheable());
        assertEquals(TerminalType.IB.name(), decision.terminalCode());
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<NibActivationEligibilityService> provider(
            NibActivationEligibilityService eligibilityService
    ) {
        ObjectProvider<NibActivationEligibilityService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(eligibilityService);
        return provider;
    }
}
