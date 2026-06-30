package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.delivery;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model.PwaOAuth2AccessToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * Legacy token delivery context kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public final class LegacyTokenDeliveryContext {
    private final LegacyClientType clientType;
    private final RegisteredClient registeredClient;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final PwaOAuth2AccessToken token;

    public LegacyTokenDeliveryContext(
            LegacyClientType clientType,
            RegisteredClient registeredClient,
            HttpServletRequest request,
            HttpServletResponse response,
            PwaOAuth2AccessToken token
    ) {
        this.clientType = clientType;
        this.registeredClient = registeredClient;
        this.request = request;
        this.response = response;
        this.token = token;
    }

    public LegacyClientType clientType() {
        return clientType;
    }

    public RegisteredClient registeredClient() {
        return registeredClient;
    }

    public HttpServletRequest request() {
        return request;
    }

    public HttpServletResponse response() {
        return response;
    }

    public PwaOAuth2AccessToken token() {
        return token;
    }
}
