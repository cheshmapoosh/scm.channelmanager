package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;

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
    protected boolean support(ScmService service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return terminal.isSupportCheckAuthentication() &&
                service.getCheckAccessFirstAuthentication();
    }

}
