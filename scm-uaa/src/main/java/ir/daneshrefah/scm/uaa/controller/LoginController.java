package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Controller
public class LoginController {

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

        boolean isError = null != error;
        boolean isStepTwoRequired = checkIsStepTwoRequired(request);
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//        return (token != null) ? Collections.singletonMap(token.getParameterName(), token.getToken())
//                : Collections.emptyMap();
        model.addAttribute("client_id", clientId);
        model.addAttribute("csrf_name", token.getParameterName());
        model.addAttribute("csrf_value", token.getToken());
        model.addAttribute("isError", isError);
        model.addAttribute("isStepTwoRequired", isStepTwoRequired);
//        Employee employee = new Employee();
//        model.addAttribute("employee", employee);
        return "login";
    }

    private boolean checkIsStepTwoRequired(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        return null != exception && TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass());
    }

}
