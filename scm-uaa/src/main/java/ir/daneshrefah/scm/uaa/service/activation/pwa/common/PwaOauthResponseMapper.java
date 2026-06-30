package ir.daneshrefah.scm.uaa.service.activation.pwa.common;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationResponse;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(name = "activationDataSource")
@RequiredArgsConstructor
@Slf4j
public class PwaOauthResponseMapper {

    private final ResourceBundleService bundle;
    private static final String MESSAGE_PREFIX = "info.message.";

    public ActivationResponse map(Exception exception) {
        if (exception instanceof GeneralPwaOauthException generalException) {
            PwaOauthMessage exceptionMessage = generalException.getExceptionMessage();
            if (PwaOauthMessage.REACHED_LOGIN_LIMIT.equals(exceptionMessage)) {
                return getMessage(PwaOauthMessage.REACHED_LOGIN_LIMIT);
            }
            return getMessage(generalException.getExceptionMessage());
        } else {
            log.error("PWA OAuth response mapping failed: {}", safeMessage(exception));
            return getMessage(PwaOauthMessage.CLIENT_REGISTRATION_FAILED);
        }
    }

    public ActivationResponse getMessage(PwaOauthMessage exceptionMessage) {
        var bundleKey = MESSAGE_PREFIX + exceptionMessage.name().toLowerCase();
        ActivationResponse response = new ActivationResponse();
        response.setText(bundle.get(AccessibleLocale.EN_US.getLocale(), bundleKey).orElse(exceptionMessage.getText()));
        response.setCode(exceptionMessage.getHttpStatus().value());
        response.setMessageKey(exceptionMessage.name());
        response.setHttpCode(exceptionMessage.getHttpStatus());
        response.setDetail(bundle.get(AccessibleLocale.FA_IR.getLocale(), bundleKey).orElse(StringUtils.EMPTY));
        return response;
    }

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

}
