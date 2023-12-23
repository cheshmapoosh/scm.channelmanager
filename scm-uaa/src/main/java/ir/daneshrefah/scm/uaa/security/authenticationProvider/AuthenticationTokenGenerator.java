package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class AuthenticationTokenGenerator {

    public AbstractAuthenticationToken generateToken(PreAuthenticationToken authentication, TerminalUserDetails userDetails) {

        AuthenticationMethod authenticationMethod = extractAuthenticationMethod(authentication.getGrantType(),
                userDetails.getUser());

        AbstractAuthenticationToken result = null;

        return result;
    }

    private AuthenticationMethod extractAuthenticationMethod(AuthorizationGrantType grantType, User user) {
        AuthenticationMethod toTest;
        if (AuthorizationGrantType.FIRST_PASSWORD.equals(grantType)) {
            toTest = user.getLoginAuthenticationMethod();
        } else if (AuthorizationGrantType.SECOND_PASSWORD.equals(grantType)) {
            toTest = user.getTransactionAuthenticationMethod();
        } else {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNSUPPORTED_TOKEN_TYPE);
        }
        return toTest;
    }
}
