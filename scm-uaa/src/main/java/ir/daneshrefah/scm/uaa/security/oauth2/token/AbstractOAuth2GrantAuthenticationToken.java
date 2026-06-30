package ir.daneshrefah.scm.uaa.security.oauth2.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Setter
@Getter
public abstract class AbstractOAuth2GrantAuthenticationToken extends org.springframework.security.authentication.AbstractAuthenticationToken {

    private String accessParameter;
    private String clientId;
    private String clientVersion;
    private String clientSignature;
    private String activationCode;
    private RegisteredClient registeredClient;
    private final Authentication clientPrincipal;
    private final Set<String> scopes;
//    private boolean sessionRequired;
//    private boolean notificationRequired;

    public AbstractOAuth2GrantAuthenticationToken(Set<String> scopes, Authentication clientPrincipal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.clientPrincipal = clientPrincipal;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public abstract AuthorizationGrantType getGrantType();

    @Override
    public abstract Object getPrincipal();

    @Override
    public abstract String getCredentials();

    public boolean includeChallengeCode() {
        return false;
    }

}
