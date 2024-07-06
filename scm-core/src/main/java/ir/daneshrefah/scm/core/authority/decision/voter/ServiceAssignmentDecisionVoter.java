package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.utils.MessageInputContext;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_SERVICE_NOT_ASSIGNED_TO_USER;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_SERVICE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class ServiceAssignmentDecisionVoter extends BaseAssignmentVoter {

    public ServiceAssignmentDecisionVoter(PersonProfileLoader personProfileLoader) {
        super(personProfileLoader);
    }

    @Override
    protected int vote(UserProfile profile, Service service, String asset) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        profile = fillServiceAccessForProfile(profile, terminal.getCode());
        boolean isServiceAssigned = profile.hasServiceAccess(terminal.getCode(),
                service.getCode(), asset);
        if (isServiceAssigned) {
            return ACCESS_ABSTAIN;
        }
        throw new AccessDeniedException(SCM_PARAMETER_SERVICE, ERROR_CODE_SERVICE_NOT_ASSIGNED_TO_USER, "service not assigned.");
    }

    @Override
    protected boolean support(Service service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return terminal.isSupportCheckServiceAccess() &&
                service.getCheckAccessService();
    }

}
