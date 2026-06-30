package ir.daneshrefah.scm.uaa.security.oauth2.error;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_PASSWORD;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_USER;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_IS_DISABLED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_IS_EXPIRED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_IS_LOCKED;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_REQUIRED_CLAIM;

@Component
public class OAuth2AuthenticationErrorMapper {
    public String parameterName(Exception exception) {
        if (exception == null) {
            return OAUTH2_ERROR_CODE_INVALID_USER;
        }
        boolean isStepTwo = exception instanceof TwoStepAuthenticationRequiredException;
        exception = exception instanceof TwoStepAuthenticationRequiredException && exception.getCause() != null
                ? (Exception) exception.getCause()
                : exception;
        if (exception instanceof LockedException) {
            return OAUTH2_ERROR_CODE_IS_LOCKED;
        }
        if (exception instanceof DisabledException) {
            return OAUTH2_ERROR_CODE_IS_DISABLED;
        }
        if (exception instanceof AccountExpiredException) {
            return OAUTH2_ERROR_CODE_IS_EXPIRED;
        }
        if (exception instanceof BadCredentialsException && isStepTwo) {
            return OAUTH2_ERROR_CODE_INVALID_CLAIM;
        }
        if (exception instanceof BadCredentialsException) {
            return OAUTH2_ERROR_CODE_INVALID_PASSWORD;
        }
        if (exception instanceof UsernameNotFoundException) {
            return OAUTH2_ERROR_CODE_INVALID_USER;
        }
        if (exception instanceof TwoStepAuthenticationRequiredException) {
            return OAUTH2_ERROR_CODE_REQUIRED_CLAIM;
        }
        if (exception instanceof BaseAuthenticationException baseAuthenticationException) {
            return baseAuthenticationException.getErrorCode();
        }
        String message = exception.getMessage();
        if (StringUtils.isEmpty(message) && exception.getCause() != null) {
            message = exception.getCause().getMessage();
        }
        return StringUtils.isEmpty(message) ? exception.getClass().getSimpleName() : safeMessage(message);
    }

    public String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return throwable == null ? null : throwable.getClass().getSimpleName();
        }
        return safeMessage(throwable.getMessage());
    }

    public String safeMessage(String message) {
        if (message == null) {
            return null;
        }
        String sanitized = message
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number|cookie|mobile|national[_-]?code)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return sanitized.length() > 300 ? sanitized.substring(0, 300) : sanitized;
    }
}
