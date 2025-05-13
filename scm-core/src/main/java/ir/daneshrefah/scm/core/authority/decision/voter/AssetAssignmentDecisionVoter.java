package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.utils.MessageInputContext;
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
    protected int vote(UserProfile profile, Service service, String asset) {
        AbstractAuditableExternalServiceProvider provider = service instanceof AbstractAuditableExternalService ?
                ((AbstractAuditableExternalService<?>) service).getServiceProvider() : null;
        if (Objects.isNull(provider) || Objects.isNull(provider.getAssetProvider())) {
            return ACCESS_ABSTAIN;
        }
        if (StringUtils.isEmpty(asset)) {
            throw new AccessDeniedException(SCM_PARAMETER_ASSET, ERROR_CODE_ASSET_IS_EMPTY, "asset must not be empty.");
        }
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        profile = personProfileLoader.preparePersonProfileMemberships(profile, terminal.getCode());
        boolean isAssetAssigned = profile.hasAssetAccess(provider.getAssetProvider().getId(), asset);
        if (!isAssetAssigned) {
            throw new AccessDeniedException(SCM_PARAMETER_ASSET, ERROR_CODE_ASSET_NOT_ASSIGNED, "asset not assigned.");
        }
        return ACCESS_ABSTAIN;
    }

    @Override
    protected boolean support(Service service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return terminal.isSupportCheckServiceAccess() &&
                service.getCheckAccessService();
    }

}
