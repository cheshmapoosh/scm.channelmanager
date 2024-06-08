package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.common.model.message.ClientAuthenticationType;
import ir.daneshrefah.scm.uaa.client.provider.token.AnonymousAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
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

    /*public AnonymousAuthenticationToken convertByHttpRequest(String terminalCode, HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return convertByHeader(null, terminalCode, header);
    }*/

    /*@Override
    public AnonymousAuthenticationToken convertByHeader(String username, String terminalCode, String authorizationHeader) {
        if (StringUtils.isNotEmpty(authorizationHeader)) {
            return null;
        }
        return new AnonymousAuthenticationToken(
                "scm_anonymous", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }*/

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        if (null != request && !ClientAuthenticationType.ANONYMOUS.equals(request.getAuthenticationType())) {
            return null;
        }
        return new AnonymousAuthenticationToken(
                "scm_anonymous", request.getTerminalCode(), request.getClientId(), request.getAccessParameter(),
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

}
