package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-27
 */
@Getter
public class OAuth2ShahkarAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String phoneNumber;
    private final String credentials;
    private Instant lastUsedAt;
    private final Instant createdAt;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal contains national code of user.
     * @param phoneNumber contains phoneNumber of user.
     * @param credentials contains claim code.
     *
     */
    public OAuth2ShahkarAuthenticationToken(Object principal, String phoneNumber, String credentials,
                                            Set<String> scopes, Authentication clientPrincipal) {
        this(principal, phoneNumber, credentials, scopes, clientPrincipal, null, Instant.now(),Instant.now());
    }

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal contains national code of user.
     * @param phoneNumber contains phoneNumber of user.
     * @param credentials contains claim code.
     *
     */
    public OAuth2ShahkarAuthenticationToken(Object principal, String phoneNumber, String credentials,
                                            Set<String> scopes, Authentication clientPrincipal, Collection<? extends GrantedAuthority> authorities,Instant createdAt,Instant lastUsedAt) {
        super(scopes, clientPrincipal, authorities);
        this.principal = principal;
        this.phoneNumber = phoneNumber;
        this.credentials = credentials;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        if (Objects.nonNull(authorities) && !authorities.isEmpty()) {
            setAuthenticated(true);
        }
    }

    @Override
    public AuthorizationGrantType getGrantType() {
        return AuthorizationGrantType.SHAHKAR;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }



}
