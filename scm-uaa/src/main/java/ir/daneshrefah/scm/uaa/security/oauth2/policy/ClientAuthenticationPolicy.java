package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.exception.ClientAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ClientAuthenticationPolicy {
    public void validate(PreAuthenticationToken authentication) {
        RegisteredClient registeredClient = authentication.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new ClientAuthenticationRequiredException();
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)
                && !AuthenticationUtils.isFullyAuthenticated()) {
            throw new ClientAuthenticationRequiredException();
        }
    }
}
