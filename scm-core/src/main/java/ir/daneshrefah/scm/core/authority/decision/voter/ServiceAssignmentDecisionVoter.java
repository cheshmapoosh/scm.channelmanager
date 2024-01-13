package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.model.person.PersonProfile;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class ServiceAssignmentDecisionVoter extends BaseAssignmentVoter {

    public ServiceAssignmentDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected int vote(PersonProfile personProfile, Service service, Object asset) {
        boolean isServiceAssigned = personProfile.hasServiceAccess(service.getCode(), asset);
        return isServiceAssigned ? ACCESS_ABSTAIN : ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckServiceAccess() &&
                service.getTerminalServiceAccess().getService().getCheckAccessService();
    }

}
