package ir.daneshrefah.scm.core.authority.decision.voter;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
@RequiredArgsConstructor
public abstract class BaseAssignmentVoter extends DecisionVoter {

    private static final String DEFAULT_ASSET_PROPERTY = "accountNo";
    private final DecisionHelper decisionHelper;

    @Override
    protected final int vote(Message message) {
        UserProfile profile = message.getHeader().getUserProfile();
        if (null == profile) {
            return ACCESS_DENIED;
        }

        String asset = getAssetValue(message);

        return vote(profile, message.getHeader().getServiceAccess(), asset);
    }

    protected final UserProfile fillServiceAccessForProfile(UserProfile profile, String terminalCode) {
        return decisionHelper.fillServiceAccessForProfile(profile, terminalCode);
    }

    protected abstract int vote(UserProfile profile, TerminalServiceAccess service, String asset);

    private boolean isAssetSupport(Message message) {
        return message.getHeader().getServiceAccess().getTerminal().isSupportCheckServiceAccess() &&
                message.getHeader().getServiceAccess().getService().getCheckAccessAsset();
    }

    private String getAssetValue(Message message) {
        if (!isAssetSupport(message)) {
            return null;
        }
        String assetProperty = message.getHeader().getServiceAccess().getService().getAssetProperty();
        if (StringUtils.isEmpty(assetProperty)) {
            assetProperty = DEFAULT_ASSET_PROPERTY;
        }
        if (StringUtils.isEmpty(assetProperty)) {
            return null;
        }
        if (null == message.getPayload() || message.getPayload().isNull() || !message.getPayload().has(assetProperty)) {
            return null;
        }
        JsonNode assetNode = message.getPayload().get(assetProperty);
        if (assetNode.isNumber()) {
            return String.valueOf(assetNode.asLong());
        } else if (assetNode.isBoolean()) {
            return String.valueOf(assetNode.asBoolean());
        } else {
            return assetNode.asText();
        }
    }

}
