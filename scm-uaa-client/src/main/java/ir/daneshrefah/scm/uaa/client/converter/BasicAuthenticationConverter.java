package ir.daneshrefah.scm.uaa.client.converter;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BasicAuthenticationConverter extends org.springframework.security.web.authentication.www.BasicAuthenticationConverter {

    public UsernamePasswordAuthenticationToken convert(String header) {
        if (header == null) {
            return null;
        }
        header = header.trim();
        if (!StringUtils.startsWithIgnoreCase(header, AUTHENTICATION_SCHEME_BASIC)) {
            return null;
        }
        if (header.equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {
            throw new BadCredentialsException("Empty basic authentication token");
        }
        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded = decode(base64Token);
        String token = new String(decoded, getCredentialsCharset());
        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken
                .unauthenticated(token.substring(0, delim), token.substring(delim + 1));
//TODO        result.setDetails(this.getAuthenticationDetailsSource().buildDetails(request));
        return result;
    }

    private byte[] decode(byte[] base64Token) {
        try {
            return Base64.getDecoder().decode(base64Token);
        }
        catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }
    }

}
