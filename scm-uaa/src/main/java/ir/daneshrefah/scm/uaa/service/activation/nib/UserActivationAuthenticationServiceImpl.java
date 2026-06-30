package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_ACTIVATION;

@Service
@ConditionalOnBean(name = "activationDataSource")
@Slf4j
@RequiredArgsConstructor
public class UserActivationAuthenticationServiceImpl implements UserActivationAuthenticationService {

    private final UserService userService;

    @Override
    public AuthenticationStatus checkAuthentication(String username, TerminalType fromTerminal, TerminalType toTerminal) {
        if (userService.loadUserEntityByUsername(username, TerminalType.NIB.name()).isPresent()) {
            return AuthenticationStatus.ACTIVATED_BEFORE;
        }
        if (userService.loadUserEntityByUsername(username, fromTerminal.name()).isPresent()) {
            return AuthenticationStatus.READY_FOR_AUTHENTICATE;
        }
        return AuthenticationStatus.USER_NOT_FOUND;
    }

    @Override
    public CandidateStatus checkActivationCandidate(PreAuthenticationToken preAuthenticationToken) {
        Set<String> scopes = preAuthenticationToken.getScopes();
        if (Objects.nonNull(scopes) && !scopes.isEmpty()) {
            String activationTerminal = preAuthenticationToken.getActivatorTerminal();
            if (scopes.contains(OAUTH2_SCOPE_NAME_ACTIVATION)){
                if (TerminalType.fromCode(activationTerminal).isEmpty()) {
                    log.debug("Invalid Activation scope for user '{}'", preAuthenticationToken.getName());
                    return CandidateStatus.HAS_ERROR;
                }
                return CandidateStatus.ACCEPTED ;
            }
            return CandidateStatus.REJECTED;
        }
        return CandidateStatus.REJECTED;
    }
}
