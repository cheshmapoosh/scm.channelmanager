package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class ServiceAssignmentDecisionVoter extends DecisionVoter {

    @Override
    protected int vote(Message message) {
        return ACCESS_ABSTAIN;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckServiceAccess() &&
                service.getTerminalServiceAccess().getService().getCheckAccessService();
    }

}
