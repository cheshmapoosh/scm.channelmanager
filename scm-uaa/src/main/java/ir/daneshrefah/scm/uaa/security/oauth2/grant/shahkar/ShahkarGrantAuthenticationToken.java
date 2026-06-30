package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.token.AbstractOAuth2GrantAuthenticationToken;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Getter
public class ShahkarGrantAuthenticationToken extends AbstractOAuth2GrantAuthenticationToken {
    private final Object principal;
    private final String phoneNumber;
    private final String credentials;
    private final String appVersion;
    private final Instant createdAt;
    private Instant lastUsedAt;

    public ShahkarGrantAuthenticationToken(
            Object principal,
            String phoneNumber,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal,
            String appVersion
    ) {
        this(
                principal,
                phoneNumber,
                credentials,
                scopes,
                clientPrincipal,
                appVersion,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    public ShahkarGrantAuthenticationToken(
            Object principal,
            String phoneNumber,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal,
            String appVersion,
            Collection<? extends GrantedAuthority> authorities,
            Instant createdAt,
            Instant lastUsedAt
    ) {
        super(scopes, clientPrincipal, authorities);
        this.principal = principal;
        this.phoneNumber = phoneNumber;
        this.credentials = credentials;
        this.appVersion = appVersion;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        if (authorities != null && !authorities.isEmpty()) {
            setAuthenticated(true);
        }
    }

    @Override
    public AuthorizationGrantType getGrantType() {
        return AuthorizationGrantType.SHAHKAR;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = Objects.requireNonNull(lastUsedAt, "lastUsedAt must not be null");
    }
}
