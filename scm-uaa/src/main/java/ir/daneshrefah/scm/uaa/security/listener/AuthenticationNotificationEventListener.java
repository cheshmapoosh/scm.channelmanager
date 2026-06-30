package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@RequiredArgsConstructor
@Component
@SuppressWarnings("deprecation")
public class AuthenticationNotificationEventListener extends BaseAuthenticationListener {

    private final TerminalService terminalService;
    private final NotificationService notificationService;

    @Override
    protected void onSuccessAuthenticationEvent(AuthenticationOutcomeToken authentication) {
        if (!authentication.isNotificationRequired()) {
            return;
        }
        User user = authentication.getPrincipal().getUser();
        terminalService
                .findTerminalByCode(user.getTerminalCode())
                .ifPresent(terminal -> {
                    notificationService.sendNotification(extractNotificationRequest(authentication, terminal));
                });
    }

    private NotificationRequest extractNotificationRequest(AuthenticationOutcomeToken authentication, Terminal terminal) {
        User user = authentication.getPrincipal().getUser();
        NotificationData data = new NotificationData()
                .put(NotificationDataKey.LOGIN_TIME, convertToPersianNumber(getShamsiLoginTime(authentication)))
                .put(NotificationDataKey.TERMINAL_TITLE, terminal.getTitle());
        IssuerInfo issuerInfo = IssuerInfo.builder()
                .parentCorrelationId(authentication.getSessionId())
                .terminalCode(user.getTerminalCode())
                .build();
        Recipient recipient = Recipient.builder()
                .address(user.getPerson().getMobile1())
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminal.getCode())
                .build();
        NotificationRequest request = NotificationRequest.builder()
                .template(NotificationTemplate.AUTHENTICATION)
                .media(NotificationMedia.SMS)
                .recipient(recipient)
                .userLocale(Locale.forLanguageTag("fa-IR"))
                .data(data)
                .terminalCode(user.getTerminalCode())
                .issuerInfo(issuerInfo)
                .build();
        return request;
    }

    private String convertToPersianNumber(String value) {
        if (value == null) return null;
        char[] persianDigits = {'۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'};
        StringBuilder result = new StringBuilder();
        for (char ch : value.toCharArray()) {
            if (Character.isDigit(ch)) {
                result.append(persianDigits[ch - '0']);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private String getShamsiLoginTime(AuthenticationOutcomeToken authentication) {
        return DateUtils
                .ShamsiCalendarConvertor
                .convertToShamsiDateString(DateUtils
                        .DateConverter
                        .convertToLocalDateTime(DateUtils.DateConverter
                                .convertToTimestamp(authentication.getIssuedAt())), "yyyy/MM/dd-HH:mm");
    }


}
