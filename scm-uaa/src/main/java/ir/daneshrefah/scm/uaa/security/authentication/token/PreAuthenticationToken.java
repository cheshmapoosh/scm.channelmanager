package ir.daneshrefah.scm.uaa.security.authentication.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.token.AbstractOAuth2GrantAuthenticationToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Getter@Setter
public class PreAuthenticationToken extends AbstractOAuth2GrantAuthenticationToken {

    private String username;
    private String password;
    private String claimCode;
    private AuthorizationGrantType grantType;
    private String remoteAddress;
    private String activatorTerminal;
    /* (DEFAULT GRANT TYPE) PWA PRE AUTHENTICATION TOKEN DATA */
    private DefaultGrantPreAuthenticationToken defaultGrantPreAuthToken;

    /**
     * if in user authentication time, client doesn't authenticate. user must send client's id
     * */

    public PreAuthenticationToken(String username, String password, AuthorizationGrantType grantType,
                                  Authentication clientPrincipal, Set<String> scopes, Object details) {
        super(scopes, clientPrincipal, Collections.emptyList());
        setDetails(details);
        this.username = username;
        this.password = password;
        this.grantType = grantType;
    }

    public boolean hasDefaultGrantPreAuthToken() {
        return Objects.nonNull(defaultGrantPreAuthToken);
    }

    @Override
    public String getCredentials() {
        return password;
    }

    @Override
    public String getPrincipal() {
        return username;
    }

}
