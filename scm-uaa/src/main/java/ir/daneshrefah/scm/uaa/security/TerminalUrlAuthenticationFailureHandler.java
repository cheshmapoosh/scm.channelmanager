package ir.daneshrefah.scm.uaa.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class TerminalUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String defaultFailureUrl;

    public TerminalUrlAuthenticationFailureHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
        this.defaultFailureUrl = defaultFailureUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (this.defaultFailureUrl == null) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Sending 401 Unauthorized error since no failure URL is set");
            } else {
                this.logger.debug("Sending 401 Unauthorized error");
            }
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
            return;
        }
        saveException(request, exception);
        if (this.isUseForward()) {
            this.logger.debug("Forwarding to " + this.defaultFailureUrl);
            request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
        } else {
            String redirectUrl = UriComponentsBuilder.fromUriString(this.defaultFailureUrl)
                    .queryParam(OAuth2ParameterNames.CLIENT_ID, request.getParameter(OAuth2ParameterNames.CLIENT_ID))
                    .toUriString();
            this.getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }


}
