package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.authority.decision.constant.Vote;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Component
@RequiredArgsConstructor
public class AuthorizationSecurityProvider extends SecurityProvider {

    private static final String JWT = "jwt";

    @Override
    protected Vote apply(Exchange exchange) {
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        Jwt jwt = (Jwt) exchange.getIn().getHeader(JWT);
        List<String> userRoles = getUserRoles(jwt);
        List<String> serviceRoles = baseChannelServiceDefinition.getAccessRoles();
        return serviceRoles
                .stream()
                .filter(userRoles::contains)
                .map(role -> Vote.ACCESS_ABSTAIN)
                .findFirst()
                .orElseThrow(()-> new AccessDeniedException("access user", ERROR_CODE_ACCESS_DENIED, "User does not have access to the target service."));
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

    @Override
    protected boolean support(Exchange exchange) {
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        return Objects.nonNull(baseChannelServiceDefinition.getAccessRoles()) && !baseChannelServiceDefinition.getAccessRoles().isEmpty();
    }
}
