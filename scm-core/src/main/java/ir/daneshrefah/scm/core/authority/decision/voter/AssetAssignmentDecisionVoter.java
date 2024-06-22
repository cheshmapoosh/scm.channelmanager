package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Objects;

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

    public AssetAssignmentDecisionVoter(PersonProfileLoader personProfileLoader) {
        super(personProfileLoader);
    }

    @Override
    protected int vote(UserProfile profile, TerminalServiceAccess service, String asset) {
        ExternalServiceProvider provider = service.getService() instanceof ExternalService ?
                ((ExternalService) service.getService()).getServiceProvider() : null;
        if (Objects.isNull(provider) || Objects.isNull(provider.getAssetProvider())) {
            return ACCESS_ABSTAIN;
        }
        if (StringUtils.isEmpty(asset)) {
            throw new AccessDeniedException(SCM_PARAMETER_ASSET, ERROR_CODE_ASSET_IS_EMPTY, "asset must not be empty.");
        }
        profile = personProfileLoader.preparePersonProfileMemberships(profile, service.getTerminal().getCode());
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
