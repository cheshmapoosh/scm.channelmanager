package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.exception.activation.InvalidActivationTerminalCodeException;
import ir.daneshrefah.scm.uaa.exception.activation.UserActivatedBeforeException;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.nib.UserActivationAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivationPolicy {
    private final UserActivationAuthenticationService activationAuthenticationService;

    public ActivationDecision decide(PreAuthenticationToken authentication, String clientTerminalCode) {
        UserActivationAuthenticationService.CandidateStatus candidateStatus =
                activationAuthenticationService.checkActivationCandidate(authentication);
        if (UserActivationAuthenticationService.CandidateStatus.ACCEPTED.equals(candidateStatus)) {
            UserActivationAuthenticationService.AuthenticationStatus authenticationStatus = activationAuthenticationService
                    .checkAuthentication(
                            authentication.getName(),
                            TerminalType.fromCode(authentication.getActivatorTerminal()).orElse(null),
                            TerminalType.NIB
                    );
            return switch (authenticationStatus) {
                case USER_NOT_FOUND -> throw new UsernameNotFoundException("Invalid username or password");
                case ACTIVATED_BEFORE -> throw new UserActivatedBeforeException();
                default -> new ActivationDecision(authentication.getName(), authentication.getActivatorTerminal(), false);
            };
        }
        if (UserActivationAuthenticationService.CandidateStatus.HAS_ERROR.equals(candidateStatus)) {
            throw new InvalidActivationTerminalCodeException();
        }
        return new ActivationDecision(authentication.getName(), clientTerminalCode, true);
    }

    public record ActivationDecision(String username, String terminalCode, boolean cacheable) {
    }
}
