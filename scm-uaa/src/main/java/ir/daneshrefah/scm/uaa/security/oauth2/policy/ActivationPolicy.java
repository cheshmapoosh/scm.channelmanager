package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.exception.activation.InvalidActivationTerminalCodeException;
import ir.daneshrefah.scm.uaa.exception.activation.UserActivatedBeforeException;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.nib.ActivationCandidateRequest;
import ir.daneshrefah.scm.uaa.service.activation.nib.NibActivationEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivationPolicy {
    private final ObjectProvider<NibActivationEligibilityService> eligibilityServiceProvider;

    public ActivationDecision decide(PreAuthenticationToken authentication, String clientTerminalCode) {
        NibActivationEligibilityService eligibilityService = eligibilityServiceProvider.getIfAvailable();
        if (eligibilityService == null) {
            return new ActivationDecision(authentication.getName(), clientTerminalCode, true);
        }
        ActivationCandidateRequest request = new ActivationCandidateRequest(
                authentication.getName(),
                authentication.getScopes(),
                TerminalType.fromCode(authentication.getActivatorTerminal()).orElse(null),
                TerminalType.NIB
        );
        NibActivationEligibilityService.CandidateStatus candidateStatus = eligibilityService.checkCandidate(request);
        if (NibActivationEligibilityService.CandidateStatus.ACCEPTED.equals(candidateStatus)) {
            NibActivationEligibilityService.AuthenticationStatus authenticationStatus =
                    eligibilityService.checkAuthentication(request);
            return switch (authenticationStatus) {
                case USER_NOT_FOUND -> throw new UsernameNotFoundException("Invalid username or password");
                case ACTIVATED_BEFORE -> throw new UserActivatedBeforeException();
                default -> new ActivationDecision(authentication.getName(), authentication.getActivatorTerminal(), false);
            };
        }
        if (NibActivationEligibilityService.CandidateStatus.HAS_ERROR.equals(candidateStatus)) {
            throw new InvalidActivationTerminalCodeException();
        }
        return new ActivationDecision(authentication.getName(), clientTerminalCode, true);
    }

    public record ActivationDecision(String username, String terminalCode, boolean cacheable) {
    }
}
