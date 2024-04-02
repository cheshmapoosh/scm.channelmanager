package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ASSET_IS_EMPTY;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ASSET_NOT_ASSIGNED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ASSET;

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
    protected int vote(PersonProfile profile, TerminalServiceAccess service, String asset) {
        ExternalServiceProvider provider = service.getService() instanceof ExternalService ?
                ((ExternalService) service.getService()).getServiceProvider() : null;
        if (null == provider || !provider.isCustomerProvided()) {
            return ACCESS_ABSTAIN;
        }
        if (null == asset) {
            throw new AccessDeniedException(SCM_PARAMETER_ASSET, ERROR_CODE_ASSET_IS_EMPTY, "asset must not be empty.");
        }
        boolean isAssetAssigned = profile.hasAssetAccess(provider.getId(), asset, null);
        if (!isAssetAssigned) {
            throw new AccessDeniedException(SCM_PARAMETER_ASSET, ERROR_CODE_ASSET_NOT_ASSIGNED, "asset not assigned.");
        }
        return ACCESS_ABSTAIN;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return serviceAccess.getTerminal().isSupportCheckAssetAccess() &&
                serviceAccess.getService().getCheckAccessAsset();
    }

}
