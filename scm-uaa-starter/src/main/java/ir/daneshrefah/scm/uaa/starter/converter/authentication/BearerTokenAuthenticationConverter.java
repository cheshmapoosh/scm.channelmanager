package ir.daneshrefah.scm.uaa.starter.converter.authentication;

import ir.daneshrefah.scm.uaa.starter.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.uaa.starter.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;

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
        if (null == request || !TokenType.BEARER.equals(request.getTokenType())) {
            return null;
        }

        if (StringUtils.isNotEmpty(request.getAuthenticationValue())) {
            BearerAuthenticationToken authenticationRequest = new BearerAuthenticationToken(
                    request.getUsername(), request.getTerminalCode(), request.getClientId(), request.getAccessParameter(),
                    request.getAuthenticationValue());
            return authenticationRequest;
        }

        return null;
    }

}
