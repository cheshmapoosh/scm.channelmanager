package ir.daneshrefah.scm.uaa.client.converter;

import ir.daneshrefah.scm.uaa.client.provider.token.SessionAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class SessionKeyResolver {

    private static final Pattern authorizationPattern = Pattern.compile("^Session (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    public SessionAuthenticationToken resolve(String authorizationHeader) {
        final String authorizationHeaderToken = resolveFromAuthorizationHeader(authorizationHeader);
        final String parameterToken = null; /*isParameterTokenSupportedForRequest(request)
                ? resolveFromRequestParameters(request) : null;*/
        if (authorizationHeaderToken != null) {
            if (parameterToken != null) {
                final BearerTokenError error = BearerTokenErrors
                        .invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }

            SessionAuthenticationToken authenticationRequest = new SessionAuthenticationToken(authorizationHeaderToken);
//            authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));


            return authenticationRequest;
        }
        /*if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
            return parameterToken;
        }*/
        return null;
    }

    private String resolveFromAuthorizationHeader(String authorization) {
        if (!StringUtils.startsWithIgnoreCase(authorization, "Session")) {
            return null;
        }
        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Session key is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

}
