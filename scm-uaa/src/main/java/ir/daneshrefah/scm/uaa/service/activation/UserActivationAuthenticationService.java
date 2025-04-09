package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;

public interface UserActivationAuthenticationService {

    AuthenticationStatus checkAuthentication(String username, TerminalCodes fromTerminal, TerminalCodes toTerminal);

    CandidateStatus checkActivationCandidate(PreAuthenticationToken preAuthenticationToken);

    enum CandidateStatus {
        ACCEPTED,
        REJECTED,
        HAS_ERROR
    }

    enum AuthenticationStatus {
        READY_FOR_AUTHENTICATE,
        ACTIVATED_BEFORE,
        USER_NOT_FOUND
    }
}
