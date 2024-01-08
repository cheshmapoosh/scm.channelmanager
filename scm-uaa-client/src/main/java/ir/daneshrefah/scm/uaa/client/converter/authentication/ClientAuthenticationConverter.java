package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationType;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class ClientAuthenticationConverter extends org.springframework.security.web.authentication.www.BasicAuthenticationConverter
        implements AuthenticationConverter {

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        if (null == request || !ClientAuthenticationType.BASIC.equals(request.getAuthenticationType())) {
            return null;
        }

        String terminalCode = request.getTerminalCode();
        byte[] base64Token = null != request.getAuthenticationValue() ?
                request.getAuthenticationValue().getBytes(StandardCharsets.UTF_8) : null;
        ClientAuthenticationToken result = null;
        try {
            byte[] decoded = decoded = decode(base64Token);
            String token = new String(decoded, getCredentialsCharset());
            int delim = token.indexOf(":");
            if (delim == -1) {
                throw new BadCredentialsException("Invalid basic authentication token");
            }
            String clientId = token.substring(0, delim);
            if (StringUtils.isEmpty(terminalCode)) {
                terminalCode = clientId;
            }
            result = ClientAuthenticationToken
                    .unauthenticated(terminalCode, clientId, token.substring(delim + 1));
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid basic authentication token", e);
        }
        return result;
    }

    private byte[] decode(byte[] base64Token) {
        try {
            return Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }
    }

}
