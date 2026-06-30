package ir.daneshrefah.scm.uaa.security.authentication.method;

import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class LoginAuthenticationTokenFactory {

    public Optional<Class<? extends UserLoginAuthenticationToken>> tokenType(
            PreAuthenticationToken authentication,
            TerminalUserDetails userDetails
    ) {
        AuthenticationMethod authenticationMethod = authenticationMethod(
                authentication.getGrantType(),
                userDetails
        );
        AuthorizationGrantType effectiveGrantType = effectiveGrantType(authentication.getGrantType());
        boolean claimCodeProvided = StringUtils.isNotEmpty(authentication.getClaimCode());

        return Arrays.stream(LoginAuthenticationTokenTypes.values())
                .filter(type -> type.getGrantType().equals(effectiveGrantType))
                .filter(type -> type.isClaimCodeProvided() == claimCodeProvided)
                .filter(type -> type.getAuthenticationMethod().equals(authenticationMethod))
                .map(LoginAuthenticationTokenTypes::getTokenClass)
                .findFirst();
    }

    public UserLoginAuthenticationToken create(
            PreAuthenticationToken authentication,
            TerminalUserDetails userDetails
    ) {
        Class<? extends UserLoginAuthenticationToken> tokenType = tokenType(authentication, userDetails)
                .orElse(null);
        if (tokenType == null) {
            return null;
        }
        try {
            return tokenType
                    .getDeclaredConstructor(TerminalUserDetails.class, PreAuthenticationToken.class)
                    .newInstance(userDetails, authentication);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot create login authentication token", rootCause(exception));
        }
    }

    private AuthorizationGrantType effectiveGrantType(AuthorizationGrantType grantType) {
        return AuthorizationGrantType.DEFAULT.equals(grantType)
                ? AuthorizationGrantType.FIRST_PASSWORD
                : grantType;
    }

    private AuthenticationMethod authenticationMethod(
            AuthorizationGrantType grantType,
            TerminalUserDetails userDetails
    ) {
        if (AuthorizationGrantType.FIRST_PASSWORD.equals(grantType)
                || AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return userDetails.getUser().getLoginAuthenticationMethod();
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNSUPPORTED_TOKEN_TYPE);
    }

    private Throwable rootCause(ReflectiveOperationException exception) {
        if (exception instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            return invocation.getCause();
        }
        return exception;
    }
}
