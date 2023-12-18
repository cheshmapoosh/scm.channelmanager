package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.mapper.AuthorizationGrantTypeMapper;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
public class FirstPasswordAuthenticationToken extends AbstractAuthenticationToken {

    private String username;
    private String password;
    private Authentication clientPrincipal;

    public FirstPasswordAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                            String username, String password,
                                            Authentication clientPrincipal) {
        super(authorities);
        this.username = username;
        this.password = password;
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
        return AuthorizationGrantTypeMapper.INSTANCE.toSpring(ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType.FIRST_PASSWORD);
    }

    public Authentication getClientPrincipal() {
        return clientPrincipal;
    }
}
