package ir.daneshrefah.scm.core.authority.decision.voter;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.model.person.PersonProfile;
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
        String personProfileId = message.getHeader().getPersonIdentifier();
        if (StringUtils.isEmpty(personProfileId)) {
            return ACCESS_DENIED;
        }

        Object asset = getAssetValue(message);

        return vote(personProfileId, message.getHeader().getService().getTerminalServiceAccess().getService(), asset);
    }

    protected final PersonProfile fetchPersonProfile(String personProfileId) {
        return decisionHelper.findPersonProfileById(personProfileId);
    }

    protected abstract int vote(String personProfileId, Service service, Object asset);

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
