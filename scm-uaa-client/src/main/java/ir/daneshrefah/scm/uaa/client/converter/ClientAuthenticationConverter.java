package ir.daneshrefah.scm.uaa.client.converter;

import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;

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
    public ClientAuthenticationToken convertByHeader(String terminalCode, String authorizationHeader) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        authorizationHeader = authorizationHeader.trim();
        if (!StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return null;
        }
        if (authorizationHeader.equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {
            throw new BadCredentialsException("Empty basic authentication token");
        }
        byte[] base64Token = authorizationHeader.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded = decode(base64Token);
        String token = new String(decoded, getCredentialsCharset());
        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        ClientAuthenticationToken result = ClientAuthenticationToken
                .unauthenticated(terminalCode, token.substring(0, delim), token.substring(delim + 1));
//TODO        result.setDetails(this.getAuthenticationDetailsSource().buildDetails(request));
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
