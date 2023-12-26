package ir.daneshrefah.scm.uaa.security.token.generator;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.security.token.AuthenticationTokenTypes;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class AuthenticationRequestTokenGenerator {

    public Optional<Class<? extends GeneralAuthenticationToken>> extractTokenType(PreAuthenticationToken authentication, TerminalUserDetails userDetails) throws Exception {
        AuthenticationMethod authenticationMethod = extractAuthenticationMethod(authentication.getGrantType(),
                userDetails.getUser());
        List<AuthenticationTokenTypes> filteredTokenType = Arrays.stream(AuthenticationTokenTypes.values()).filter(
                        authenticationTokenType -> authenticationTokenType.getGrantType().equals(authentication.getGrantType()) &&
                                authenticationTokenType.isClaimCodeProvided() == StringUtils.isNotEmpty(authentication.getClaimCode()) &&
                                authenticationTokenType.getAuthenticationMethod().equals(authenticationMethod))
                .collect(Collectors.toList());
        if (filteredTokenType.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(filteredTokenType.get(0).getTokenClass());
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
        token.setDetails(userDetails);
        return token;
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
