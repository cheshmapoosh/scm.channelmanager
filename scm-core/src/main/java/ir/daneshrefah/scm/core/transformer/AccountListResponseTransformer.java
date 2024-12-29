package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AccountListResponseTransformer extends AbstractJsonTransformer {

    private final PersonProfileLoader personProfileLoader;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (!(payload instanceof ArrayNode sourceArray)) {
            log.warn(">>> account list payload is not an array");
            return null;
        }
        ArrayNode result = JsonNodeFactory.instance.arrayNode();

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(AuthenticationUtils.getScmAuthentication());

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
        log.info(">>> {} memberships found ", memberships.size());
        final String accountNumber = "accountNumber";
        if (sourceNode.has(accountNumber)) {
            String accountNo = sourceNode.get(accountNumber).asText();
            Optional<MembershipTerminalAccess> membership = memberships.stream()
                    .filter(m ->
                    accountNo.equals(m.getMembership().getCustomerAccount().getAccount().getAccountNo())
            ).findFirst();
            if (membership.isEmpty() || !membership.get().getActive() ) {
                membership
                        .ifPresentOrElse(membershipTerminalAccess -> log.warn(">>>>> membership active status is : [{}]  ", membershipTerminalAccess.getActive())
                        ,()-> log.warn(">>>>> membership found status : [{}]  ", membership.isEmpty()));
                sourceNode.put("nickName", StringUtils.EMPTY);
                sourceNode.put("favorite", StringUtils.EMPTY);
                return null;
            }else {
                MembershipTerminalAccess membershipTerminalAccess = membership.get();
                String nickname = membershipTerminalAccess.getMembership().getNickname();
                sourceNode.put("nickName", Objects.nonNull(nickname) ? nickname : StringUtils.EMPTY);
                checkingAccountFavoriteStatus(sourceNode,membershipTerminalAccess);
            }
            return sourceNode;
        }
        log.warn(">>>>> account not found");
        return null;
    }

    private void checkingAccountFavoriteStatus(ObjectNode sourceNode,MembershipTerminalAccess membershipTerminalAccess) {
        Boolean favorite = membershipTerminalAccess.getFavorite();
        favorite = !Objects.isNull(favorite) && favorite;
        sourceNode.put("favorite",favorite);
    }

}
