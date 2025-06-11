package ir.daneshrefah.scm.core.integration.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.constant.AccountStatus;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountListEnricherPlugin implements PluginHandler {

    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String ACCOUNT_STATUS = "accountStatusCode";
    private final PersonProfileLoader personProfileLoader;
    private final ObjectMapper objectMapper;

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        PluginPhase phase = pluginDetail.getPhase();
        if (!PluginPhase.AFTER.equals(phase)) {
            throw new IllegalArgumentException("Plugin phase " + phase + " is not supported on 'accountListEnricherPlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        String jsonBody = String.valueOf(exchange.getIn().getBody());
        JsonNode body = objectMapper.readTree(jsonBody);
        JsonNode payload = body.get("result");
        log.info(">>> account list transformer loading");
        if ((payload instanceof ArrayNode sourceArray)) {
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
            ((ObjectNode)body).set("result", result);
            exchange.getIn().setBody(body);
        }else {
            log.warn(">>> account list payload is not an array");
        }
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
