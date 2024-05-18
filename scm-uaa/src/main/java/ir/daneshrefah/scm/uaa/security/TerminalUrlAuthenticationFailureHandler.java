package ir.daneshrefah.scm.uaa.security;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.SESSION_KEY_IS_STEP_TWO;

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
        checkIsStepTwoRequired(request);
        if (this.isUseForward()) {
            this.logger.debug("Forwarding to " + this.defaultFailureUrl);
            request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
        } else {
            this.getRedirectStrategy().sendRedirect(request, response, this.defaultFailureUrl);
//            String redirectUrl = UriComponentsBuilder.fromUriString(this.defaultFailureUrl)
//                    .queryParam(OAuth2ParameterNames.CLIENT_ID, request.getParameter(OAuth2ParameterNames.CLIENT_ID))
//                    .toUriString();
//            this.getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    private void checkIsStepTwoRequired(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        boolean isStepTwoRequired = null != exception && TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass());
        if (isStepTwoRequired) {
            request.getSession().setAttribute(SESSION_KEY_IS_STEP_TWO, true);
        } else {
            request.getSession().removeAttribute(SESSION_KEY_IS_STEP_TWO);
        }
    }

}
