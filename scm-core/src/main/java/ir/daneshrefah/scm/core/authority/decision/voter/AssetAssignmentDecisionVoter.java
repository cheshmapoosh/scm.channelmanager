package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;

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
    protected int vote(String personProfileId, Service service, Object asset) {
        if (null == asset) {
            return ACCESS_DENIED;
        }
        String serviceProviderId = service instanceof ExternalService ? ((ExternalService) service).getServiceProvider().getId() : null;
        PersonProfile personProfile = fetchPersonProfile(personProfileId);
        boolean isAssetAssigned = personProfile.hasAssetAccess(serviceProviderId, asset);
        return isAssetAssigned ? ACCESS_ABSTAIN : ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return service.getTerminalServiceAccess().getTerminal().getSupportCheckAssetAccess() &&
                service.getTerminalServiceAccess().getService().getCheckAccessAsset();
    }

}
