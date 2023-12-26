package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class AnonymousAuthenticationConverter extends org.springframework.security.web.authentication.www.BasicAuthenticationConverter
        implements AuthenticationConverter {

    public AnonymousAuthenticationToken convertByHttpRequest(String terminalCode, HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return convertByHeader(terminalCode, header);
    }

    @Override
    public AnonymousAuthenticationToken convertByHeader(String terminalCode, String authorizationHeader) {
        if (StringUtils.isNotEmpty(authorizationHeader)) {
            return null;
        }
        return new AnonymousAuthenticationToken(
                "scm_anonymous", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

}
