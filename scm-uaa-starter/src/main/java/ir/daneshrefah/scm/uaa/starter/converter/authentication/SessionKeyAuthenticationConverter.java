package ir.daneshrefah.scm.uaa.starter.converter.authentication;

import ir.daneshrefah.scm.uaa.starter.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.uaa.starter.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.SessionAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class SessionKeyAuthenticationConverter implements AuthenticationConverter {

    private static final Pattern authorizationPattern = Pattern.compile("^Session (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    /*@Override
    public SessionAuthenticationToken convertByHeader(String username, String terminalCode, String authorizationHeader) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        final String authorizationHeaderToken = resolveFromAuthorizationHeader(authorizationHeader);
        final String parameterToken = null; *//*isParameterTokenSupportedForRequest(request)
                ? resolveFromRequestParameters(request) : null;*//*
        if (authorizationHeaderToken != null) {
            if (parameterToken != null) {
                final BearerTokenError error = BearerTokenErrors
                        .invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }

            SessionAuthenticationToken authenticationRequest = new SessionAuthenticationToken(username, terminalCode,
                    authorizationHeaderToken);
//            authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));


            return authenticationRequest;
        }
        *//*if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
            return parameterToken;
        }*//*
        return null;
    }*/

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        if (null == request || !TokenType.SESSION.equals(request.getTokenType())) {
            return null;
        }

        if (StringUtils.isNotEmpty(request.getAuthenticationValue())) {
            SessionAuthenticationToken authenticationRequest = new SessionAuthenticationToken(request.getUsername(),
                    request.getTerminalCode(), request.getClientId(), request.getAccessParameter(), request.getAuthenticationValue());
            return authenticationRequest;
        }

        return null;
    }

/*    private String resolveFromAuthorizationHeader(String authorization) {
        if (!StringUtils.startsWithIgnoreCase(authorization, "Session")) {
            return null;
        }
        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Session key is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }*/

}
