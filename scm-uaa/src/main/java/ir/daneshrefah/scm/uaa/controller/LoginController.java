package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final ClientService clientService;
    private final MessageSource messageSource;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request,
                        @RequestParam(name = "client_id", required = false) String clientId,
                        @RequestParam(name = "error", required = false) String error) {
        String errorMessage = null;
        Client client = clientService.findByClientId(clientId).orElseThrow(() -> new InvalidInvocationException("client_id"));
        boolean isError = null != error;
        if (isError) {
            errorMessage = extractErrorMessage(request);
        }
        boolean isStepTwoRequired = checkIsStepTwoRequired(request);
        AuthenticationMethod authenticationMethod = null;
        Instant otpExpireTime = null;
        if (isStepTwoRequired) {
            authenticationMethod = extractAuthenticationMethod(request);
            otpExpireTime = extractOtpExpireTime(request);
        }
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//        return (token != null) ? Collections.singletonMap(token.getParameterName(), token.getToken())
//                : Collections.emptyMap();
        model.addAttribute("client_title", client.getTitle());
        model.addAttribute("client_id", clientId);
        model.addAttribute("csrf_name", token.getParameterName());
        model.addAttribute("csrf_value", token.getToken());
        model.addAttribute("isError", isError);
        model.addAttribute("isStepTwoRequired", isStepTwoRequired);
        model.addAttribute("errorMessageCode", errorMessage);
        if (StringUtils.isNotEmpty(errorMessage)) {
            String messagekey = "message.error." + errorMessage;
            model.addAttribute("errorMessage", messageSource.getMessage(messagekey, null, "?" + messagekey + "?", null));
        }
        model.addAttribute("authenticationMethod", null != authenticationMethod ? authenticationMethod.getCode() : null);
        model.addAttribute("otpExpireTime", otpExpireTime);
//        Employee employee = new Employee();
//        model.addAttribute("employee", employee);
        return "login";
    }

    @PostMapping("/login-cancel")
    public String cancelLogin(Model model, HttpServletRequest request,
                        @RequestParam(name = "client_id", required = false) String clientId,
                        @RequestParam(name = "error", required = false) String error) {

        Client client = clientService.findByClientId(clientId).orElseThrow(() -> new InvalidInvocationException("client_id"));
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        request.getSession().invalidate();
        model.addAttribute("client_title", client.getTitle());
        model.addAttribute("client_id", clientId);
        model.addAttribute("csrf_name", token.getParameterName());
        model.addAttribute("csrf_value", token.getToken());

        return "login";
    }

    private Instant extractOtpExpireTime(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (null == exception || !TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass()) ||
                null != exception.getCause()) {
            return null;
        }
        PostAuthenticationToken authenticationToken = (PostAuthenticationToken) ((TwoStepAuthenticationRequiredException) exception).getAuthentication();
        return null != authenticationToken.getOtpSendResponse() ? authenticationToken.getOtpSendResponse().getExpireTime() : null;
    }

    private String extractErrorMessage(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (null == exception ||
                (TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass()) && null == exception.getCause())) {
            return null;
        }
        boolean isStepTwo = exception instanceof TwoStepAuthenticationRequiredException;
        Exception messageException = exception instanceof TwoStepAuthenticationRequiredException ? (Exception) exception.getCause() : exception;
        if (messageException instanceof LockedException)
            return OAUTH2_ERROR_CODE_IS_LOCKED;
        else if (messageException instanceof DisabledException)
            return OAUTH2_ERROR_CODE_IS_DISABLED;
        else if (messageException instanceof AccountExpiredException)
            return OAUTH2_ERROR_CODE_IS_EXPIRED;
        else if (messageException instanceof BadCredentialsException && isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_CLAIM;
        else if (messageException instanceof BadCredentialsException && !isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_PASSWORD;
        else if (messageException instanceof UsernameNotFoundException)
            return OAUTH2_ERROR_CODE_INVALID_USER;
        else if (messageException instanceof TwoStepAuthenticationRequiredException)
            return OAUTH2_ERROR_CODE_REQUIRED_CLAIM;
        else if (messageException instanceof BaseAuthenticationException)
            return ((BaseAuthenticationException) exception).getErrorCode();

        return exception.getMessage();
    }

    private boolean checkIsStepTwoRequired(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        return null != exception && TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass());
    }

    private AuthenticationMethod extractAuthenticationMethod(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (null == exception || !TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass())) {
            return null;
        }
        TwoStepAuthenticationRequiredException authenticationException = (TwoStepAuthenticationRequiredException) exception;
        return ((TerminalUserDetails) authenticationException.getAuthentication().getPrincipal()).getUser().getLoginAuthenticationMethod();
    }

}
