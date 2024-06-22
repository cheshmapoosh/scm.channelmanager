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
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
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
public class AuthenticationNotificationEventListener extends BaseAuthenticationListener {

    private final TerminalService terminalService;
    private final NotificationService notificationService;

    @Override
    protected void onSuccessAuthenticationEvent(PostAuthenticationToken authentication) {
        if (!authentication.isNotificationRequired()) {
            return;
        }
//        BeanUtils.describe(authentication);
        User user = authentication.getPrincipal().getUser();
        terminalService
                .findTerminalByCode(user.getTerminalCode())
                .ifPresent(terminal -> {
                    notificationService.sendNotification(extractNotificationRequest(authentication, terminal));
                });
    }

    private NotificationRequest extractNotificationRequest(PostAuthenticationToken authentication, Terminal terminal) {
        User user = authentication.getPrincipal().getUser();
        NotificationData data = new NotificationData()
                .put(NotificationDataKey.LOGIN_TIME, getShamsiLoginTime(authentication))
                .put(NotificationDataKey.TERMINAL_TITLE, terminal.getTitle())
                .put(NotificationDataKey.OTP_CODE, "123");
        IssuerInfo issuerInfo = IssuerInfo.builder()
                .parentCorrelationId(authentication.getSessionId())
//                            .nickname(user.getNickname())
//                            .username(Objects.nonNull(user.getPerson()) ? user.getPerson().getUsername() : null)
//                            .personType(user.getPerson().getPersonType())
                .terminalCode(user.getTerminalCode())
//                            .hostAddress()
//                            .instanceName()
                .build();
        Recipient recipient = Recipient.builder()
                .address(user.getPerson().getMobile1())
//                .authenticationLevel(AuthenticationLevel.CM_AUTHENTICATED)
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminal.getCode())
// TODO                .accessParameter(authentication.getA)
                .build();
        NotificationRequest request = NotificationRequest.builder()
                .template(NotificationTemplate.AUTHENTICATION)
                .media(NotificationMedia.SMS)
                .recipient(recipient)
                .userLocale(new Locale("fa","IR")) //TODO GET FROM HEADER
                .data(data)
                .terminalCode(user.getTerminalCode())
                .issuerInfo(issuerInfo)
                .build();
        return request;
    }

    private String getShamsiLoginTime(PostAuthenticationToken authentication) {
        return DateUtils
                .ShamsiCalendarConvertor
                .convertToShamsiDateString(DateUtils
                        .DateConverter
                        .convertToLocalDateTime(DateUtils.DateConverter
                                .convertToTimestamp(authentication.getIssuedAt())), "yyyy/MM/dd HH:mm:ss");
    }


}
