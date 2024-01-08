package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

public class TransactionAuthenticationDecisionVoter extends DecisionVoter {

    @Override
    public int vote(Message message) {
        return (message.getHeader().isTransactionAuthenticated()) ? ACCESS_ABSTAIN : ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckSecondAuthentication() &&
                service.getTerminalServiceAccess().getService().getCheckAccessSecondAuthentication();
    }

}
