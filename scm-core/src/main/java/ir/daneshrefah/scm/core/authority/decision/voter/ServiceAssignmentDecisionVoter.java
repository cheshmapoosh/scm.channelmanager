package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;

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
    protected int vote(UserProfile profile, TerminalServiceAccess service, String asset) {
        profile = fillServiceAccessForProfile(profile, service.getTerminal().getCode());
        boolean isServiceAssigned = profile.hasServiceAccess(service.getTerminal().getCode(),
                service.getService().getCode(), asset);
        if (isServiceAssigned) {
            return ACCESS_ABSTAIN;
        }
        throw new AccessDeniedException(SCM_PARAMETER_SERVICE, ERROR_CODE_SERVICE_NOT_ASSIGNED_TO_USER, "service not assigned.");
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckServiceAccess() &&
                serviceAccess.getService().getCheckAccessService();
    }

}
