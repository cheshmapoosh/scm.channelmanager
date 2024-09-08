package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

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
    @Setter
    @Getter
    private String remoteAddress;

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

    @Override
    public String getCredentials() {
        return password;
    }

    @Override
    public String getPrincipal() {
        return username;
    }

}
