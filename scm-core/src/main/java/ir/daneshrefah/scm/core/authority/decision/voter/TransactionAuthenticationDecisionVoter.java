package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_AUTHENTICATION_TRANSACTION_REQUIRED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_AUTHENTICATION;

public class TransactionAuthenticationDecisionVoter extends DecisionVoter {

    @Override
    public int vote(Message message) {
        if (message.getHeader().getIsTransactionAuthenticated()) {
            return ACCESS_ABSTAIN;
        }
        throw new AccessDeniedException(SCM_PARAMETER_AUTHENTICATION, ERROR_CODE_AUTHENTICATION_TRANSACTION_REQUIRED,
                "transaction authentication required.");
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckSecondAuthentication() &&
                serviceAccess.getService().getCheckAccessSecondAuthentication();
    }

}
