package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class AssetAssignmentDecisionVoter extends BaseAssignmentVoter {

    public AssetAssignmentDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected int vote(PersonProfile profile, Service service, Object asset) {
        if (null == asset) {
            return ACCESS_DENIED;
        }
        ExternalServiceProvider serviceProvider = service instanceof ExternalService ? ((ExternalService) service).getServiceProvider() : null;
        profile = fillCustomerForProfile(profile, serviceProvider);
        boolean isAssetAssigned = profile.hasAssetAccess(null != serviceProvider ? serviceProvider.getId() : null, asset);
        return isAssetAssigned ? ACCESS_ABSTAIN : ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckAssetAccess() &&
                service.getTerminalServiceAccess().getService().getCheckAccessAsset();
    }

}
