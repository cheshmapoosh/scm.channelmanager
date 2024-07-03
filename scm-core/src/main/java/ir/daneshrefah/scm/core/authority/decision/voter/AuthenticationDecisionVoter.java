package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_AUTHENTICATION_REQUIRED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_AUTHENTICATION;

public class AuthenticationDecisionVoter extends DecisionVoter {

    @Override
    protected int vote(Message message) {
        if (AuthenticationUtils.isFullyAuthenticated()) {
            return ACCESS_ABSTAIN;
        }
        throw new AccessDeniedException(SCM_PARAMETER_AUTHENTICATION, ERROR_CODE_AUTHENTICATION_REQUIRED,
                "authentication required.");
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckAuthentication() &&
                serviceAccess.getService().getCheckAccessFirstAuthentication();
    }

}
