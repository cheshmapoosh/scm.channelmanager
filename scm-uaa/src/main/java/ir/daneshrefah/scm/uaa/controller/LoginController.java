package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.utils.date.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request,
                        @RequestParam(name = "client_id", required = false) String clientId,
                        @RequestParam(name = "error", required = false) String error) {
//        authenticationUrl
//        usernameParameter
//        passwordParameter

//        if (!isError) {
//            return "";
//        }
//        return "<div class=\"alert alert-danger\" role=\"alert\">" + HtmlUtils.htmlEscape(message) + "</div>";

//        if (!isLogoutSuccess) {
//            return "";
//        }
//        return "<div class=\"alert alert-success\" role=\"alert\">You have been signed out</div>";

        String errorMessage = null;
        Client client = clientService.findByClientId(clientId);
        boolean isError = null != error;
        if (isError) {
            errorMessage = extractErrorMessage(request);
        }
        boolean isStepTwoRequired = checkIsStepTwoRequired(request);
        AuthenticationMethod authenticationMethod = null;
        if (isStepTwoRequired) {
            authenticationMethod = extractAuthenticationMethod(request);
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
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("authenticationMethod", null != authenticationMethod ? authenticationMethod.getCode() : null);
        model.addAttribute("otpExpireTime", DateUtils.InstantTools.plusSecondsToCurrent(120));
//        Employee employee = new Employee();
//        model.addAttribute("employee", employee);
        return "login";
    }

    private String extractErrorMessage(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (null == exception ||
                (TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass()) && null == exception.getCause())) {
            return null;
        }
        boolean isStepTwo = exception instanceof TwoStepAuthenticationRequiredException;
        Exception messageException = exception instanceof TwoStepAuthenticationRequiredException ? (Exception) exception.getCause() : exception;
        if (exception instanceof LockedException)
            return OAUTH2_ERROR_CODE_IS_LOCKED;
        else if (exception instanceof DisabledException)
            return OAUTH2_ERROR_CODE_IS_DISABLED;
        else if (exception instanceof AccountExpiredException)
            return OAUTH2_ERROR_CODE_IS_EXPIRED;
        else if (exception instanceof BadCredentialsException && isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_CLAIM;
        else if (exception instanceof BadCredentialsException && !isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_PASSWORD;
        else if (exception instanceof UsernameNotFoundException)
            return OAUTH2_ERROR_CODE_INVALID_USER;
        else if (exception instanceof TwoStepAuthenticationRequiredException)
            return OAUTH2_ERROR_CODE_REQUIRED_CLAIM;
        else if (exception instanceof BaseAuthenticationException)
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
