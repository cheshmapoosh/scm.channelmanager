package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

public class AuthenticationDecisionVoter extends DecisionVoter {

    @Override
    protected int vote(Message message) {
        return (message.getHeader().getAuthentication().isAuthenticated() &&
                !message.getHeader().getAuthentication().isAnonymous()) ? ACCESS_ABSTAIN : ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckAuthentication() &&
                service.getTerminalServiceAccess().getService().getCheckAccessFirstAuthentication();
    }

}
