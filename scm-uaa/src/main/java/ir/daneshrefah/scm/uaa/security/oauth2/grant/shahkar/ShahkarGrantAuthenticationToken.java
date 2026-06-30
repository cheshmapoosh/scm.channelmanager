package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

public class ShahkarGrantAuthenticationToken extends OAuth2ShahkarAuthenticationToken {
    public ShahkarGrantAuthenticationToken(
            Object principal,
            String phoneNumber,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal
    ) {
        super(principal, phoneNumber, credentials, scopes, clientPrincipal);
    }

    public ShahkarGrantAuthenticationToken(
            Object principal,
            String phoneNumber,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal,
            Collection<? extends GrantedAuthority> authorities,
            Instant createdAt,
            Instant lastUsedAt
    ) {
        super(principal, phoneNumber, credentials, scopes, clientPrincipal, authorities, createdAt, lastUsedAt);
    }
}
