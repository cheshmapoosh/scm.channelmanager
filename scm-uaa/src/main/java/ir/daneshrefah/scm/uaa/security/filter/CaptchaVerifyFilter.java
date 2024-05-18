package ir.daneshrefah.scm.uaa.security.filter;

import ir.daneshrefah.scm.uaa.common.exception.CaptchaVerifyException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.SESSION_KEY_IS_STEP_TWO;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-11
 */
public class CaptchaVerifyFilter extends OncePerRequestFilter {

    private final String SESSION_KEY = "loginCaptchaKey";
//    private final String sessionKey;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final RequestMatcher requiresAuthenticationRequestMatcher;

    public CaptchaVerifyFilter(String loginProcessingUrl, AuthenticationFailureHandler authenticationFailureHandler) {
        this.requiresAuthenticationRequestMatcher = new AntPathRequestMatcher(loginProcessingUrl, "POST");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }
        String captcha = request.getParameter("captcha");
        String captchaV = (String) request.getSession().getAttribute(SESSION_KEY);
        if (StringUtils.notEqualsIgnoreCase(captchaV, captcha)) {
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, new CaptchaVerifyException());
            return;
        }
        chain.doFilter(request, response);
    }

    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Boolean isStepTwo = (Boolean) request.getSession().getAttribute(SESSION_KEY_IS_STEP_TWO);
        if (Objects.isNull(isStepTwo)) {
            isStepTwo = false;
        }
        if (!isStepTwo && this.requiresAuthenticationRequestMatcher.matches(request)) {
            return true;
        }
        if (this.logger.isTraceEnabled()) {
            this.logger
                    .trace(LogMessage.format("Did not match request to %s", this.requiresAuthenticationRequestMatcher));
        }
        return false;
    }
}
