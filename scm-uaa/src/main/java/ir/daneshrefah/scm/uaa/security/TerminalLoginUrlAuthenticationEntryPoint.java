package ir.daneshrefah.scm.uaa.security;

import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-17
 */
@Deprecated
public class TerminalLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    /**
     * @param loginFormUrl URL where the login page can be found. Should either be
     *                     relative to the web-app context path (include a leading {@code /}) or an absolute
     *                     URL.
     */
    public TerminalLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {
        String loginForm = super.determineUrlToUseForThisRequest(request, response, exception);
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (StringUtils.isEmpty(clientId)) {
            clientId = "SCM";
        }
        return UriComponentsBuilder.fromUriString(loginForm)
                .queryParam(OAuth2ParameterNames.CLIENT_ID, clientId)
                .toUriString();
    }
}
