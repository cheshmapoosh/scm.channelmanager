package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;

/**
 * Spring Authentication token kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyPasswordGrantAuthenticationToken extends PreAuthenticationToken {
    private final LegacyPasswordGrantRequest request;

    public LegacyPasswordGrantAuthenticationToken(LegacyPasswordGrantRequest request) {
        super(
                request.username(),
                request.password(),
                request.grantType(),
                request.clientPrincipal(),
                request.scopes(),
                null
        );
        this.request = request;
        setClientId(request.clientId());
        setRemoteAddress(request.remoteAddress());
    }

    public LegacyPasswordGrantRequest request() {
        return request;
    }

    public LegacyClientType legacyClientType() {
        return request.legacyClientType();
    }
}
