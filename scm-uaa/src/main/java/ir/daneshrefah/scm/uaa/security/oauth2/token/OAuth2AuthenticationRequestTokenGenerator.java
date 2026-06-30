package ir.daneshrefah.scm.uaa.security.oauth2.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.method.LoginAuthenticationTokenFactory;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationRequestTokenGenerator implements AuthenticationRequestTokenGenerator {
    private final LoginAuthenticationTokenFactory tokenFactory;

    @Override
    public Optional<Class<? extends UserLoginAuthenticationToken>> extractTokenType(
            Authentication authentication,
            TerminalUserDetails userDetails
    ) {
        Assert.isAssignable(PreAuthenticationToken.class, authentication.getClass());
        return tokenFactory.tokenType((PreAuthenticationToken) authentication, userDetails);
    }

    public UserLoginAuthenticationToken generateToken(
            PreAuthenticationToken authentication,
            TerminalUserDetails userDetails
    ) {
        return tokenFactory.create(authentication, userDetails);
    }
}
