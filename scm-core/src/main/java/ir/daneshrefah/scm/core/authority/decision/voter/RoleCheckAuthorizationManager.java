package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.authority.decision.manager.SecurityContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class RoleCheckAuthorizationManager implements AuthorizationManager<SecurityContext> {

    private static final String JWT = "jwt";

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, SecurityContext securityContext) {
        Exchange exchange = securityContext.getExchange();
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        return Optional
                .ofNullable(baseChannelServiceDefinition)
                .map(BaseChannelServiceDefinition::getAuthorizationConfig)
                .filter(config -> Objects.nonNull(config.getAccessRoles()) && !config.getAccessRoles().isEmpty())
                .map(config -> {
                    Jwt jwt = (Jwt) exchange.getIn().getHeader(JWT);
                    List<String> userRoles = getUserRoles(jwt);
                    List<String> serviceRoles = config.getAccessRoles();
                    return serviceRoles
                            .stream()
                            .filter(userRoles::contains)
                            .map(role -> new AuthorizationDecision(true))
                            .findFirst()
                            .orElseGet(() -> new AuthorizationDecision(false));
                }).orElse(null);
    }


    private List<String> getUserRoles(Jwt jwt) {
        String aut = String.valueOf(jwt.getClaims().get("aut"));
        List<String> roles = Collections.emptyList();
        if (StringUtils.isNotBlank(aut)) {
            roles = Arrays.stream(
                            aut.substring(1, aut.length() - 1)
                                    .split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
        return roles;
    }


}
