package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    @Getter
    @Setter
    private String claimCode;
    @Getter
    private AuthorizationGrantType grantType;
    @Getter
    private Authentication clientPrincipal;
    @Getter
    private final Set<String> scopes;

    public PreAuthenticationToken(String username, String password, AuthorizationGrantType grantType,
                                  Authentication clientPrincipal, Set<String> scopes) {
        super(Collections.emptyList());
        this.username = username;
        this.password = password;
        this.grantType = grantType;
        this.clientPrincipal = clientPrincipal;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }



}
