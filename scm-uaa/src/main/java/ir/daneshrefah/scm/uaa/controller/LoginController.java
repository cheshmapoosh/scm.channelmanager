package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.service.ClientService;
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

        /*switch (exception.getMessage()) {
            case Constants.OAUTH2_PARAM_NAME_USER_USERNAME:
                return Constants.OAUTH2_PARAM_NAME_USER_USERNAME;
            case Constants.OAUTH2_PARAM_NAME_USER_PASSWORD:
                return Constants.OAUTH2_PARAM_NAME_USER_PASSWORD;
            case Constants.OAUTH2_ERROR_CODE_IS_LOCKED:
                return Constants.OAUTH2_ERROR_CODE_IS_LOCKED;
            case Constants.OAUTH2_ERROR_CODE_IS_DISABLED:
                return Constants.OAUTH2_ERROR_CODE_IS_DISABLED;
            case Constants.OAUTH2_ERROR_CODE_IS_EXPIRED:
                return Constants.OAUTH2_ERROR_CODE_IS_EXPIRED;
            case Constants.OAUTH2_ERROR_CODE_INVALID_PASSWORD:
                return Constants.OAUTH2_ERROR_CODE_INVALID_PASSWORD;
            case Constants.OAUTH2_ERROR_CODE_INVALID_USER:
                return Constants.OAUTH2_ERROR_CODE_INVALID_USER;
        }*/
        return Constants.OAUTH2_PARAM_NAME_USER_USERNAME;
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
