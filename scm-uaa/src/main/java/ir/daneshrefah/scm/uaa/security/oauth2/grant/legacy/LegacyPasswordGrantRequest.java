package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import org.springframework.security.core.Authentication;

import java.util.Set;

/**
 * Legacy password request model kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public final class LegacyPasswordGrantRequest {
    private final String username;
    private final String password;
    private final AuthorizationGrantType grantType;
    private final Authentication clientPrincipal;
    private final Set<String> scopes;
    private final String clientId;
    private final String remoteAddress;
    private final LegacyClientType legacyClientType;

    public LegacyPasswordGrantRequest(
            String username,
            String password,
            AuthorizationGrantType grantType,
            Authentication clientPrincipal,
            Set<String> scopes,
            String clientId,
            String remoteAddress,
            LegacyClientType legacyClientType
    ) {
        this.username = username;
        this.password = password;
        this.grantType = grantType;
        this.clientPrincipal = clientPrincipal;
        this.scopes = scopes;
        this.clientId = clientId;
        this.remoteAddress = remoteAddress;
        this.legacyClientType = legacyClientType;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public AuthorizationGrantType grantType() {
        return grantType;
    }

    public Authentication clientPrincipal() {
        return clientPrincipal;
    }

    public Set<String> scopes() {
        return scopes;
    }

    public String clientId() {
        return clientId;
    }

    public String remoteAddress() {
        return remoteAddress;
    }

    public LegacyClientType legacyClientType() {
        return legacyClientType;
    }

    @Override
    public String toString() {
        return "LegacyPasswordGrantRequest{" +
                "username='[PROTECTED]'" +
                ", grantType=" + grantType +
                ", clientId='" + clientId + '\'' +
                ", legacyClientType=" + legacyClientType +
                '}';
    }
}
