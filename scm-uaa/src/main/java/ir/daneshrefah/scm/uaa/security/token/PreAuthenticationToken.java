package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
public class PreAuthenticationToken extends AbstractAuthenticationToken {

    private String username;
    private String password;
    private AuthorizationGrantType grantType;
    private Authentication clientPrincipal;

    public PreAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                  String username, String password, AuthorizationGrantType grantType,
                                  Authentication clientPrincipal) {
        super(authorities);
        this.username = username;
        this.password = password;
        this.grantType = grantType;
        this.clientPrincipal = clientPrincipal;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    public AuthorizationGrantType getGrantType() {
        return grantType;
    }

    public Authentication getClientPrincipal() {
        return clientPrincipal;
    }
}
