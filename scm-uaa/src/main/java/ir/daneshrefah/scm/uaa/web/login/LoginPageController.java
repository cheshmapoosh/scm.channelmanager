package ir.daneshrefah.scm.uaa.web.login;

import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.uaa.common.exception.CaptchaVerifyException;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
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
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.SESSION_KEY_IS_STEP_TWO;
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
public class LoginPageController {

    private static final String LOGIN_PAGE = "login";
    private static final String PARAMETER_KEY_CLIENT_ID = "client_id";
    private static final String PARAMETER_KEY_SPRING_SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    private static final String ATTRIBUTE_CLIENT_ID = "client_id";
    private static final String ATTRIBUTE_CLIENT_TITLE = "client_title";
    private static final String ATTRIBUTE_CSRF_NAME = "csrf_name";
    private static final String ATTRIBUTE_CSRF_VALUE = "csrf_value";
    private static final String ATTRIBUTE_IS_ERROR = "isError";
    private static final String ATTRIBUTE_IS_STEP_TWO = "isStepTwoRequired";
    private static final String ATTRIBUTE_ERROR_MESSAGE_CODE = "errorMessageCode";
    private static final String ATTRIBUTE_ERROR_MESSAGE_VALUE = "errorMessage";
    private static final String ATTRIBUTE_AUTHENTICATION_METHOD = "authenticationMethod";
    private static final String ATTRIBUTE_OTP_EXPIRE_TIME = "otpExpireTime";
    private static final String ATTRIBUTE_OPERATION = "operation";
    private static final String OPERATION_LOGIN = "operation_login";
    private static final String OPERATION_CANCEL_OTP = "operation_cancel_otp";
    private static final String OPERATION_CANCEL_LOGIN = "operation_cancel_login";
    private final ClientService clientService;
    private final MessageSource messageSource;
    private final ClientLoginThemeResolver themeResolver;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request,
                        @RequestParam(name = ATTRIBUTE_OPERATION, required = false) String operation,
                        @RequestParam(name = "error", required = false) String error) {
        if (StringUtils.isEmpty(operation)) {
            operation = OPERATION_LOGIN;
        }
        if (StringUtils.equalsIgnoreCase(OPERATION_CANCEL_OTP, operation)) {
            request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            request.getSession().removeAttribute(SESSION_KEY_IS_STEP_TWO);
//            request.getParameterMap().clear();
        }
        Optional<Client> client = extractClientInfo(request);
        String errorMessage = extractErrorMessage(request);
        boolean isError = StringUtils.isNotEmpty(errorMessage);
        AuthenticationMethod authenticationMethod = extractAuthenticationMethod(request);
        Instant otpExpireTime = extractOtpExpireTime(request);
        boolean isStepTwoRequired = checkIsStepTwoRequired(request);
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        model.addAttribute(ATTRIBUTE_OPERATION, OPERATION_LOGIN);
        model.addAttribute(ATTRIBUTE_CLIENT_ID, client.map(Client::getUser).map(User::getNickname).orElse(null));
        model.addAttribute(ATTRIBUTE_CLIENT_TITLE, client.map(Client::getUser).map(User::getPerson).filter(generalPerson -> generalPerson instanceof GeneralLegalPerson).map(GeneralLegalPerson.class::cast).map(GeneralLegalPerson::getTitle).orElse("invalid_client"));
        model.addAttribute(ATTRIBUTE_CSRF_NAME, null != token ? token.getParameterName() : null);
        model.addAttribute(ATTRIBUTE_CSRF_VALUE, null != token ? token.getToken() : null);
        model.addAttribute(ATTRIBUTE_IS_ERROR, isError);
        model.addAttribute(ATTRIBUTE_IS_STEP_TWO, isStepTwoRequired);
        model.addAttribute(ATTRIBUTE_ERROR_MESSAGE_CODE, errorMessage);
        if (StringUtils.isNotEmpty(errorMessage)) {
            String messageKey = "message.error." + errorMessage;
            model.addAttribute(ATTRIBUTE_ERROR_MESSAGE_VALUE, messageSource.getMessage(messageKey, null, "?" + messageKey + "?", null));
        }
        model.addAttribute(ATTRIBUTE_AUTHENTICATION_METHOD, null != authenticationMethod ? authenticationMethod.getCode() : null);
        model.addAttribute(ATTRIBUTE_OTP_EXPIRE_TIME, otpExpireTime);
        model.addAttribute("loginTheme", themeResolver.resolve(client.map(Client::getUser).map(User::getNickname).orElse(null)));

        return LOGIN_PAGE;
    }

    private Optional<Client> extractClientInfo(HttpServletRequest request) {
        SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute(PARAMETER_KEY_SPRING_SAVED_REQUEST);
        if (Objects.isNull(savedRequest)) {
            return Optional.empty();
        }
        if (!savedRequest.getParameterMap().containsKey(PARAMETER_KEY_CLIENT_ID) ||
                Objects.isNull(savedRequest.getParameterMap().get(PARAMETER_KEY_CLIENT_ID)) ||
                savedRequest.getParameterMap().get(PARAMETER_KEY_CLIENT_ID).length < 1) {
            return Optional.empty();
        }
        String clientId = savedRequest.getParameterMap().get(PARAMETER_KEY_CLIENT_ID)[0];
        return clientService.findByNickname(clientId);
    }

    @PostMapping("/login-cancel")
    public String cancelLogin(Model model, HttpServletRequest request,
                        @RequestParam(name = "client_id", required = false) String clientId,
                        @RequestParam(name = "error", required = false) String error) {

        Client client = clientService.findByNickname(clientId).orElseThrow(() -> new InvalidInvocationException("client_id"));
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        request.getSession().invalidate();
        GeneralLegalPerson person = (GeneralLegalPerson) client.getUser().getPerson();
        model.addAttribute("client_title", person.getTitleEnglish());
        model.addAttribute("client_id", clientId);
        model.addAttribute("csrf_name", token.getParameterName());
        model.addAttribute("csrf_value", token.getToken());

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
        else if (messageException instanceof TwoStepAuthenticationRequiredException)
            return OAUTH2_ERROR_CODE_REQUIRED_CLAIM;
        else if (messageException instanceof CaptchaVerifyException)
            return OAUTH2_ERROR_CODE_INVALID_CAPTCHA;
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

    private Instant extractOtpExpireTime(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (null == exception || !TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass()) ||
                null != exception.getCause()) {
            return null;
        }
        AuthenticationOutcomeToken authenticationToken = (AuthenticationOutcomeToken) ((TwoStepAuthenticationRequiredException) exception).getAuthentication();
        return null != authenticationToken.getOtpSendResponse() ? authenticationToken.getOtpSendResponse().getOtp().getExpireTime() : null;
    }

}
