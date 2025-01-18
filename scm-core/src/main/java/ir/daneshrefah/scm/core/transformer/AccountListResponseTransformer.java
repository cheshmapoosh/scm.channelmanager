package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.constant.AccountStatus;
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

import java.time.LocalDate;
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

    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String ACCOUNT_STATUS = "accountStatusCode";

    private final PersonProfileLoader personProfileLoader;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        log.info(">>> account list transformer loading");
        if (!(payload instanceof ArrayNode sourceArray)) {
            log.warn(">>> account list payload is not an array");
            return null;
        }
        ArrayNode result = JsonNodeFactory.instance.arrayNode();

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(AuthenticationUtils.getScmAuthentication());

        for (JsonNode sourceNode : sourceArray) {
            if (!sourceNode.isObject() || sourceNode.isEmpty()) {
                log.info(">>> account list source node :: isObject = [{}] , isEmpty = [{}] ", sourceNode.isObject(), sourceNode.isEmpty());
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
        if (isValidAccount(sourceNode)) {
            final long accountNo = sourceNode.get(ACCOUNT_NUMBER).asLong();
            Optional<MembershipTerminalAccess> membership = memberships.stream()
                    .filter(m-> LocalDate.now().isBefore(m.getToDate()))
                    .filter(m -> StringUtils.equals(
                            Long.toString(accountNo),
                            StringUtils.trim(m.getMembership().getCustomerAccount().getAccount().getAccountNo()))
                    ).findFirst();
            if (membership.isEmpty() || !membership.get().getActive()) {
                sourceNode.put("nickName", StringUtils.EMPTY);
                sourceNode.put("favorite", StringUtils.EMPTY);
                return null;
            } else {
                MembershipTerminalAccess membershipTerminalAccess = membership.get();
                String nickname = membershipTerminalAccess.getMembership().getNickname();
                sourceNode.put("nickName", Objects.nonNull(nickname) ? nickname : StringUtils.EMPTY);
                checkingAccountFavoriteStatus(sourceNode, membershipTerminalAccess);
            }
            return sourceNode;
        }
        return null;
    }

    private boolean isValidAccount(ObjectNode sourceNode) {
        if (!sourceNode.has(ACCOUNT_NUMBER) || sourceNode.get(ACCOUNT_NUMBER).isNull()){
            log.error(">>> account number field doses not found");
            return false;
        }
        if (!sourceNode.has(ACCOUNT_STATUS) || sourceNode.get(ACCOUNT_STATUS).isNull()){
            log.warn(">>> account status code does not exist for account id : {} ", sourceNode.get(ACCOUNT_STATUS).asText());
            return false;
        }
        Optional<AccountStatus> accountStatusOptional = AccountStatus.getAccountStatus(sourceNode.get(ACCOUNT_STATUS).asInt());
        if (accountStatusOptional.isEmpty() || !accountStatusOptional.get().equals(AccountStatus.ACTIVE)) {
            log.warn(">>> account status code is not 'ACTIVE' for account id : {} ", sourceNode.get(ACCOUNT_NUMBER).asText());
            return false;
        }
        return true;
    }

    private void checkingAccountFavoriteStatus(ObjectNode sourceNode, MembershipTerminalAccess membershipTerminalAccess) {
        Boolean favorite = membershipTerminalAccess.getFavorite();
        favorite = !Objects.isNull(favorite) && favorite;
        sourceNode.put("favorite", favorite);
    }

}
