package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationType;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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
public class BearerTokenAuthenticationConverter implements AuthenticationConverter {

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        if (null == request || !ClientAuthenticationType.BEARER.equals(request.getAuthenticationType())) {
            return null;
        }

        if (StringUtils.isNotEmpty(request.getAuthenticationValue())) {
            BearerAuthenticationToken authenticationRequest = new BearerAuthenticationToken(
                    request.getUsername(), request.getTerminalCode(), request.getAuthenticationValue());
            return authenticationRequest;
        }

        return null;
    }

}
