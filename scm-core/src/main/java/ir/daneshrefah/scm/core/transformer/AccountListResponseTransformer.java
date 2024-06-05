package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
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

    private final PersonProfileLoader personProfileLoader;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (null == payload || !(payload instanceof ArrayNode)) {
            return null;
        }
        ArrayNode sourceArray = (ArrayNode) payload;
        ArrayNode result = JsonNodeFactory.instance.arrayNode();

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(message.getHeader().getAuthentication());

        for (JsonNode sourceNode : sourceArray) {
            if (!sourceNode.isObject() || sourceNode.isEmpty()) {
                continue;
            }
            ObjectNode resultAccount = convertAccountNode((ObjectNode) sourceNode, profile);
            if (null != resultAccount && !resultAccount.isNull()) {
                result.add(resultAccount);
            }
        }
        return result;
    }

    private ObjectNode convertAccountNode(ObjectNode sourceNode, UserProfile profile) {
        List<MembershipTerminalAccess> memberships = profile.getMemberships();
        final String accountNumber = "accountNumber";
        if (sourceNode.has(accountNumber)) {
            String accountNo = sourceNode.get(accountNumber).asText();
            Optional<MembershipTerminalAccess> membership = memberships.stream().filter(m ->
                    accountNo.equals(m.getMembership().getCustomerAccount().getAccount().getAccountNo())
            ).findFirst();
            if (membership.isEmpty()) {
                sourceNode.put("nickName", StringUtils.EMPTY);
                return null;
            }else {
                MembershipTerminalAccess membershipTerminalAccess = membership.get();
                String nickname = membershipTerminalAccess.getMembership().getNickname();
                sourceNode.put("nickName", Objects.nonNull(nickname) ? nickname : StringUtils.EMPTY);
            }
            return sourceNode;
        }
        return null;
    }

}
