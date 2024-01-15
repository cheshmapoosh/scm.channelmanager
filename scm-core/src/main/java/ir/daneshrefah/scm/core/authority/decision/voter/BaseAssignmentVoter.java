package ir.daneshrefah.scm.core.authority.decision.voter;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
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

    private final DecisionHelper decisionHelper;

    @Override
    protected final int vote(Message message) {
        PersonProfile profile = message.getHeader().getPersonIdentifier();
        if (null == profile) {
            return ACCESS_DENIED;
        }

        Object asset = getAssetValue(message);

        return vote(profile, message.getHeader().getService().getTerminalServiceAccess(), asset);
    }

    protected final PersonProfile fillServiceAccessForProfile(PersonProfile profile, String terminalCode) {
        return decisionHelper.fillServiceAccessForProfile(profile, terminalCode);
    }

    protected final PersonProfile fillCustomerForProfile(PersonProfile profile, ExternalServiceProvider serviceProvider) {
        return decisionHelper.fillCustomerForProfile(profile, serviceProvider);
    }

    protected abstract int vote(PersonProfile profile, TerminalServiceAccess service, Object asset);

    private boolean isAssetSupport(Message message) {
        return message.getHeader().getService().getTerminalServiceAccess().getTerminal().getSupportCheckAssetAccess() &&
                message.getHeader().getService().getTerminalServiceAccess().getService().getCheckAccessAsset();
    }

    private Object getAssetValue(Message message) {
        if (!isAssetSupport(message)) {
            return null;
        }
        String assetProperty = message.getHeader().getService().getTerminalServiceAccess().getService().getAssetProperty();
        if (StringUtils.isEmpty(assetProperty)) {
            return null;
        }
        if (null == message.getPayload() || message.getPayload().isNull() || !message.getPayload().has(assetProperty)) {
            return null;
        }
        JsonNode assetNode = message.getPayload().get(assetProperty);
        if (assetNode.isNumber()) {
            return assetNode.asLong();
        } else if (assetNode.isBoolean()) {
            return assetNode.asBoolean();
        } else {
            return assetNode.asText();
        }
    }

}
