package ir.daneshrefah.scm.uaa.server.authorization.authentication;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Set;

@Getter
public class OAuth2PasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


    private final String username;
    private final String password;
    private final String clientId;
    private final Set<String> scopes;


    public OAuth2PasswordGrantAuthenticationToken(String username, String password, Authentication clientPrincipal, Set<String> scopes) {
        super(AuthorizationGrantType.PASSWORD, clientPrincipal, null);
        this.password = password;
        this.username = username;
        this.clientId = clientPrincipal.getName();
        this.scopes = scopes;
    }

}
