package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_ACTIVATION;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class NibActivationEligibilityService {
    private final NibActivationUserLookup userLookup;

    public CandidateStatus checkCandidate(ActivationCandidateRequest request) {
        if (!request.scopes().contains(OAUTH2_SCOPE_NAME_ACTIVATION)) {
            return CandidateStatus.REJECTED;
        }
        if (request.fromTerminal() == null || request.toTerminal() != TerminalType.NIB) {
            return CandidateStatus.HAS_ERROR;
        }
        return CandidateStatus.ACCEPTED;
    }

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public AuthenticationStatus checkAuthentication(ActivationCandidateRequest request) {
        if (request.toTerminal() != TerminalType.NIB) {
            throw new IllegalArgumentException("NIB activation target terminal must be NIB");
        }
        if (userLookup.existsUser(request.username(), request.toTerminal())) {
            return AuthenticationStatus.ACTIVATED_BEFORE;
        }
        if (request.fromTerminal() != null && userLookup.existsUser(request.username(), request.fromTerminal())) {
            return AuthenticationStatus.READY_FOR_AUTHENTICATE;
        }
        return AuthenticationStatus.USER_NOT_FOUND;
    }

    public enum CandidateStatus {
        ACCEPTED,
        REJECTED,
        HAS_ERROR
    }

    public enum AuthenticationStatus {
        READY_FOR_AUTHENTICATE,
        ACTIVATED_BEFORE,
        USER_NOT_FOUND
    }
}
