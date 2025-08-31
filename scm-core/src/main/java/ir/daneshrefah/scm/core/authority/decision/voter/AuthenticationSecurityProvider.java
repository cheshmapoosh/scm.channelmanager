package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.authority.decision.constant.Vote;
import ir.daneshrefah.scm.core.integration.template.context.HeaderContextResolver;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

@Component
@RequiredArgsConstructor
public class AuthenticationSecurityProvider extends SecurityProvider {

    private final AuthenticationClientTemplate authenticationClientTemplate;
    private final HeaderContextResolver headerContextResolver;
    private final PersonProfileLoader profileLoader;
    private final JwtDecoder jwtDecoder;
    private static final String AUTH_HEADER = "Authorization";

    @Override
    protected Vote apply(Exchange exchange) {
        String authValue = exchange.getIn().getHeader(AUTH_HEADER, String.class);
        if (authValue == null || !authValue.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        String token = authValue.substring("Bearer ".length());
        try {
            ClientAuthenticationRequest authenticationRequest = convertToClientAuthenticationRequest(exchange);
            UserAuthentication authentication = authenticationClientTemplate.authenticateUserByAuthenticationRequest(authenticationRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            profileLoader.preparePersonProfile(authentication);
            exchange.getIn().setHeader("jwt", jwtDecoder.decode(token));
        } catch (JwtException e) {
            throw new AuthenticationRequiredException();
        }
        // Put the decoded JWT claims in the exchange property for downstream plugins
        return Vote.ACCESS_ABSTAIN;
    }

    @Override
    protected boolean support(Exchange exchange) {
        BaseChannelServiceDefinition baseChannelServiceDefinition = (BaseChannelServiceDefinition) exchange.getProperty(Message.CHANNEL_SERVICE_DEFINITION);
        return Boolean.TRUE.equals(baseChannelServiceDefinition.getCheckLoginAuthentication());
    }

    private ClientAuthenticationRequest convertToClientAuthenticationRequest(Exchange exchange) {
        return ClientAuthenticationRequest.builder()
                .username((String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange))
                .terminalCode((String) headerContextResolver.resolve(SCM_PARAMETER_TERMINAL, exchange))
                .clientId((String) headerContextResolver.resolve(SCM_PARAMETER_CLIENT_ID, exchange))
                .tokenType(AuthenticationUtils.extractTokenType(
                        (String) headerContextResolver.resolve(SCM_PARAMETER_AUTHORIZATION, exchange),
                        (String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange),
                        (String) headerContextResolver.resolve(SCM_PARAMETER_CREDENTIAL, exchange)))
                .authenticationValue(AuthenticationUtils.extractAuthenticationValue((String) headerContextResolver.resolve(SCM_PARAMETER_AUTHORIZATION, exchange)))
                .accessParameter((String) headerContextResolver.resolve(SCM_PARAMETER_ACCESS_PARAMETER, exchange))
                .build();
    }
}
