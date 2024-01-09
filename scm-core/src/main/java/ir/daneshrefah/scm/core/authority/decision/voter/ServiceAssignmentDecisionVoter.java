package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
@RequiredArgsConstructor
public class ServiceAssignmentDecisionVoter extends DecisionVoter {

    private final DecisionHelper decisionHelper;

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
