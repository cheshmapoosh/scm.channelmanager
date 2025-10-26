package ir.daneshrefah.scm.uaa.security.token.generator;

import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.AuthenticationTokenTypes;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class OAuth2AuthenticationRequestTokenGenerator implements AuthenticationRequestTokenGenerator {

    @Override
    public Optional<Class<? extends GeneralAuthenticationToken>> extractTokenType(Authentication authentication, TerminalUserDetails userDetails) {
        Assert.isAssignable(PreAuthenticationToken.class, authentication.getClass());
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        AuthenticationMethod authenticationMethod = extractAuthenticationMethod(preAuthenticationToken.getGrantType(),
                userDetails.getUser());
        Optional<AuthenticationTokenTypes> filteredTokenType = Arrays.stream(AuthenticationTokenTypes.values()).filter(
                        authenticationTokenType -> authenticationTokenType.getGrantType().equals(getGrantType(preAuthenticationToken)) &&
                                authenticationTokenType.isClaimCodeProvided() == StringUtils.isNotEmpty(preAuthenticationToken.getClaimCode()) &&
                                authenticationTokenType.getAuthenticationMethod().equals(authenticationMethod))
                .findFirst();
        return filteredTokenType.map(AuthenticationTokenTypes::getTokenClass);
    }

    private AuthorizationGrantType getGrantType(PreAuthenticationToken preAuthenticationToken) {
        AuthorizationGrantType grantType = preAuthenticationToken.getGrantType();
        // PROXYING 'DEFAULT' GRANT TYPE ON 'FIRST_PASSWORD' ( USED ON PWA/MB )
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return AuthorizationGrantType.FIRST_PASSWORD;
        }
        return grantType;
    }

    public GeneralAuthenticationToken generateToken(PreAuthenticationToken authentication, TerminalUserDetails userDetails) throws Exception {

        Optional<Class<? extends GeneralAuthenticationToken>> tokenOptional = extractTokenType(authentication, userDetails);

        if (tokenOptional.isEmpty()) {
            return null;
        }
//TODO must change to factory instead of reflection
        GeneralAuthenticationToken token = tokenOptional.get()
                .getDeclaredConstructor(TerminalUserDetails.class, PreAuthenticationToken.class)
                .newInstance(userDetails, authentication);
        return token;
    }

    private AuthenticationMethod extractAuthenticationMethod(AuthorizationGrantType grantType, User user) {
        AuthenticationMethod toTest;
        if (AuthorizationGrantType.FIRST_PASSWORD.equals(grantType) || AuthorizationGrantType.DEFAULT.equals(grantType)) {
            toTest = user.getLoginAuthenticationMethod();
        } else if (AuthorizationGrantType.SECOND_PASSWORD.equals(grantType)) {
            toTest = user.getTransactionAuthenticationMethod();
        } else {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNSUPPORTED_TOKEN_TYPE);
        }
        return toTest;
    }
}
