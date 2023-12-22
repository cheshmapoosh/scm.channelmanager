package ir.daneshrefah.scm.uaa.client.converter;

import ir.daneshrefah.scm.uaa.client.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BearerTokenResolver implements AuthenticationConverter {

    private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public BearerAuthenticationToken convertByHeader(String terminalCode, String authorizationHeader) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        final String authorizationHeaderToken = resolveFromAuthorizationHeader(authorizationHeader);
        final String parameterToken = null; /*isParameterTokenSupportedForRequest(request)
                ? resolveFromRequestParameters(request) : null;*/
        if (authorizationHeaderToken != null) {
            if (parameterToken != null) {
                final BearerTokenError error = BearerTokenErrors
                        .invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }

            BearerAuthenticationToken authenticationRequest = new BearerAuthenticationToken(terminalCode,
                    authorizationHeaderToken);
//            authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));


            return authenticationRequest;
        }
        /*if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
            return parameterToken;
        }*/
        return null;
    }

    private String resolveFromAuthorizationHeader(String authorization) {
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null;
        }
        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

}
