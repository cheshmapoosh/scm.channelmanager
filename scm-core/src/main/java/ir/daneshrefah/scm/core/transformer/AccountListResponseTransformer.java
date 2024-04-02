package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
@RequiredArgsConstructor
@Component
public class AccountListResponseTransformer extends AbstractTransformer {

    private final CustomerService customerService;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (null == payload || !(payload instanceof ArrayNode)) {
            return null;
        }
        ArrayNode sourceArray = (ArrayNode) payload;
        ArrayNode result = JsonNodeFactory.instance.arrayNode();

        List<MembershipTerminalAccess> memberships = loadMemberships(message);

        for (JsonNode sourceNode : sourceArray) {
            if (!sourceNode.isObject() || sourceNode.isEmpty()) {
                continue;
            }
            ObjectNode resultAccount = convertAccountNode((ObjectNode) sourceNode, memberships);
            if (null != resultAccount && !resultAccount.isNull()) {
                result.add(resultAccount);
            }
        }
        return result;
    }

    private ObjectNode convertAccountNode(ObjectNode sourceNode, List<MembershipTerminalAccess> memberships) {
        String accountNo = sourceNode.get("accountNo").asText();
        Optional<MembershipTerminalAccess> membership = memberships.stream().filter(m ->
                accountNo.equals(m.getMembership().getAccount().getAccountNo())
        ).findFirst();
        if (membership.isEmpty()) {
            return null;
        }
        return JsonNodeFactory.instance.objectNode();
    }

    private List<MembershipTerminalAccess> loadMemberships(Message message) {
        if (null == message || null == message.getHeader().getAuthentication() ||
                !message.getHeader().getAuthentication().isAuthenticated()) {
            return null;
        }
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        PersonProfile profile = message.getHeader().getAuthentication().getPersonProfile();
        if (!profile.isMembershipLoaded()) {
            List<MembershipTerminalAccess> memberships = customerService.findMembershipTerminalAccessList(
                    profile.getPersonId().id(), terminal.getId());
            profile.loadMembership(memberships);
        }
        return profile.getMemberships();
    }

}
